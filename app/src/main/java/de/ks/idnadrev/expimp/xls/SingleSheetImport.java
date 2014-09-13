/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.idnadrev.expimp.xls;

import de.ks.idnadrev.expimp.DependencyGraph;
import de.ks.idnadrev.expimp.xls.result.XlsxImportSheetResult;
import de.ks.idnadrev.expimp.xls.sheet.ImportSheetHandler;
import de.ks.idnadrev.expimp.xls.sheet.ImportValue;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.entity.IdentifyableEntity;
import de.ks.reflection.ReflectionUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SingleSheetImport implements Callable<Void> {
  private static final Logger log = LoggerFactory.getLogger(SingleSheetImport.class);

  protected final ColumnProvider columnProvider;
  private final Class<?> clazz;
  private final InputStream sheetStream;
  private final DependencyGraph graph;
  private final XlsxImportSheetResult result;
  private final XlsImportCfg importCfg;
  private final EntityType<?> entityType;
  private final XSSFReader reader;

  protected final LinkedList<Runnable> runAfterImport = new LinkedList<>();

  public SingleSheetImport(Class<?> clazz, InputStream sheetStream, DependencyGraph graph, XSSFReader reader, XlsxImportSheetResult result, XlsImportCfg importCfg) {
    this.clazz = clazz;
    this.sheetStream = sheetStream;
    this.graph = graph;
    this.result = result;
    this.importCfg = importCfg;
    this.entityType = graph.getEntityType(clazz);
    this.reader = reader;
    columnProvider = new ColumnProvider(graph);
  }

  @Override
  public Void call() throws Exception {
    try {
      XMLReader parser = XMLReaderFactory.createXMLReader();
      ImportSheetHandler importSheetHandler = new ImportSheetHandler(clazz, reader.getSharedStringsTable(), columnProvider, this::importEntity);
      parser.setContentHandler(importSheetHandler);


      InputSource inputSource = new InputSource(sheetStream);
      parser.parse(inputSource);

    } catch (SAXException | IOException | InvalidFormatException e) {
      result.generalError("Failed to parse sheet " + clazz.getName(), e);
      throw new RuntimeException(e);
    } finally {
      try {
        sheetStream.close();
      } catch (IOException e) {
        result.generalError("Could not close sheet stream " + clazz.getName(), e);
        throw new RuntimeException(e);
      }
      return null;
    }
  }

  public void importEntity(List<ImportValue> importValues) {
    if (importValues.isEmpty()) {
      return;
    }


    Object idProperty;
    if (IdentifyableEntity.class.isAssignableFrom(clazz)) {
      String idPropertyName = graph.getIdentifierProperty(clazz);
      Optional<ImportValue> first = importValues.stream().filter(v -> v.getColumnDef().getIdentifier().equals(idPropertyName)).findFirst();
      idProperty = first.get().getValue();
    } else {
      idProperty = null;
    }


    PersistentWork.run(em -> {
      Object instance;
      boolean keepExisting = importCfg.isKeepExisting();
      boolean exists;

      if (idProperty != null) {
        String idPropertyName = graph.getIdentifierProperty(clazz);

        @SuppressWarnings("unchecked") IdentifyableEntity loaded = PersistentWork.findByIdentification((Class<IdentifyableEntity>) clazz, idPropertyName, idProperty);
        if (loaded != null) {
          instance = loaded;
          exists = true;
        } else {
          exists = false;
          instance = ReflectionUtil.newInstance(clazz);
        }
      } else {
        exists = false;
        instance = ReflectionUtil.newInstance(clazz);
      }
      if (exists && keepExisting) {
        result.success("Ignored " + clazz.getName() + " with identifier " + idProperty, importValues.get(0).getCellId());
        return;
      }

      List<ToOneRelationAssignment> manadatoryRelations = getManadatoryRelations(importValues, instance);
      List<ToOneRelationAssignment> optionalRelations = getOptionalRelations(importValues);
      List<ToManyRelationAssignment> toManyRelations = getToManyRelations(importValues);
      try {
        manadatoryRelations.forEach(r -> {
          r.run();
        });
        importValues.forEach(v -> {
          XlsxColumn columnDef = v.getColumnDef();
          Object value = v.getValue();
          columnDef.setValue(instance, value);
        });
        optionalRelations.forEach(r -> {
          r.ownerResolver = () -> {
            Object identifier = getIdentifier(instance);
            String identifierProperty = graph.getIdentifierProperty(clazz);
            return resolveEntity(clazz, identifierProperty, identifier);
          };
        });


        if (!exists) {
          em.persist(instance);
        }
        result.success(instance.toString(), importValues.get(0).getCellId());
        toManyRelations.forEach(r -> {
          r.ownerResolver = () -> {
            Object identifier = getIdentifier(instance);
            String identifierProperty = graph.getIdentifierProperty(instance.getClass());
            return resolveEntity(clazz, identifierProperty, identifier);
          };
        });
        runAfterImport.addAll(optionalRelations);
        runAfterImport.addAll(toManyRelations);

      } catch (Exception e) {
        result.error("Could not persist entity " + instance, e, importValues.get(0).getCellId());
        throw e;
      }
    });

  }

  private List<ToOneRelationAssignment> getManadatoryRelations(List<ImportValue> importValues, Object instance) {
    List<SingularAttribute<?, ?>> mandatoryRelations = entityType.getSingularAttributes().stream().filter(a -> a.isAssociation() && !a.isOptional()).collect(Collectors.toList());

    return mandatoryRelations.stream().map(r -> {
      Optional<ImportValue> found = importValues.stream().filter(v -> v.getColumnDef().getIdentifier().equals(r.getName())).findFirst();
      if (!found.isPresent()) {
        log.warn("Could not import {} no column definition for {}.{} found. Values: {}", clazz.getName(), r.getJavaType().getName(), r.getName(), importValues);
        result.warn("Could not import " + clazz.getName() + " no column definition for " + r.getJavaType().getName() + "." + r.getName() + " found. Values: " + importValues, null);
        return null;
      }
      ImportValue importValue = found.get();
      importValues.remove(importValue);

      String identifierProperty = graph.getIdentifierProperty(r.getJavaType());
      Consumer<Object> resultWriter = o -> result.warn("could not find association of '" + o + "' via '" + r.getName() + "' to '" + importValue.getValue() + "'", importValue.getCellId());
      return new ToOneRelationAssignment(resultWriter, () -> instance, r, importValue.getColumnDef(), identifierProperty, importValue.getValue());
    }).collect(Collectors.toList());
  }

  private List<ToOneRelationAssignment> getOptionalRelations(List<ImportValue> importValues) {
    List<SingularAttribute<?, ?>> optionalRelations = entityType.getSingularAttributes().stream().filter(a -> a.isAssociation() && a.isOptional()).collect(Collectors.toList());

    return optionalRelations.stream().map(r -> {
      Optional<ImportValue> found = importValues.stream().filter(v -> v.getColumnDef().getIdentifier().equals(r.getName())).findFirst();
      if (!found.isPresent()) {
        return null;
      }
      ImportValue importValue = found.get();
      importValues.remove(importValue);
      Consumer<Object> resultWriter = o -> result.warn("could not find association of '" + o + "' via '" + r.getName() + "' to '" + importValue.getValue() + "'", importValue.getCellId());
      String identifierProperty = graph.getIdentifierProperty(r.getJavaType());
      return new ToOneRelationAssignment(resultWriter, null, r, importValue.getColumnDef(), identifierProperty, importValue.getValue());
    }).filter(r -> r != null).collect(Collectors.toList());
  }

  private List<ToManyRelationAssignment> getToManyRelations(List<ImportValue> importValues) {
    List<PluralAttribute<?, ?, ?>> pluralAttributes = entityType.getPluralAttributes().stream().filter(a -> a.isAssociation()).collect(Collectors.toList());

    return pluralAttributes.stream().map(r -> {
      Optional<ImportValue> found = importValues.stream().filter(v -> v.getColumnDef().getIdentifier().equals(r.getName())).findFirst();
      if (!found.isPresent()) {
        return null;
      }
      ImportValue importValue = found.get();
      importValues.remove(importValue);
      String identifierProperty = graph.getIdentifierProperty(r.getElementType().getJavaType());
      return new ToManyRelationAssignment(importValue.getColumnDef(), r, identifierProperty, (String) importValue.getValue());
    }).filter(r -> r != null).collect(Collectors.toList());
  }

  static Object getIdentifier(Object instance) {
    if (IdentifyableEntity.class.isAssignableFrom(instance.getClass())) {
      return ((IdentifyableEntity) instance).getIdValue();
    }
    return null;
  }

  static Object resolveEntity(Class<?> type, String identifierProperty, Object identifier) {
    if (IdentifyableEntity.class.isAssignableFrom(type)) {
      @SuppressWarnings("unchecked") IdentifyableEntity identifyableEntity = PersistentWork.findByIdentification((Class<IdentifyableEntity>) type, identifierProperty, identifier);
      return identifyableEntity;
    }
    return null;
  }

  static List<Object> resolveToManyRelation(Class<?> type, String identifierProperty, List<String> singleIdentifiers) {
    LinkedList<Object> retval = new LinkedList<>();
    for (String identifier : singleIdentifiers) {
      Object entity = resolveEntity(type, identifierProperty, identifier);
      retval.add(entity);
    }
    return retval;
  }

  static class ToOneRelationAssignment implements Runnable {
    private final Consumer<Object> resultWriter;
    XlsxColumn ownerColumn;

    Supplier<Object> ownerResolver;
    Supplier<Object> relationResolver;

    ToOneRelationAssignment(Consumer<Object> resultWriter, Supplier<Object> ownerResolver, SingularAttribute<?, ?> relation, XlsxColumn ownerColumn, String identifierPropertyName, Object relationIdentifier) {
      this.resultWriter = resultWriter;
      this.ownerColumn = ownerColumn;
      this.ownerResolver = ownerResolver;
      relationResolver = () -> resolveEntity(relation.getJavaType(), identifierPropertyName, relationIdentifier);
    }

    @Override
    public void run() {
      PersistentWork.wrap(() -> {
        Object owner = ownerResolver.get();
        Object value = relationResolver.get();
        if (value == null) {
          resultWriter.accept(owner);
        } else {
          ownerColumn.setValue(owner, value);
        }
      });
    }
  }

  static class ToManyRelationAssignment implements Runnable {
    XlsxColumn ownerColumn;

    Supplier<Object> ownerResolver;
    Supplier<List<Object>> relationResolver;

    ToManyRelationAssignment(XlsxColumn ownerColumn, PluralAttribute relation, String identifierPropertyName, String relationString) {
      this.ownerColumn = ownerColumn;

      String[] split = StringUtils.split(relationString, "|");
      ArrayList<String> singleIdentifiers = new ArrayList<>(split.length);
      for (String string : split) {
        String id = StringUtils.replace(string, ToManyColumn.SEPARATOR, ToManyColumn.SEPARATOR_REPLACEMENT);
        singleIdentifiers.add(id);
      }
      relationResolver = () -> resolveToManyRelation(relation.getElementType().getJavaType(), identifierPropertyName, singleIdentifiers);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
      PersistentWork.wrap(() -> {
        Object owner = ownerResolver.get();
        List<Object> values = relationResolver.get();

        Collection original = (Collection) ReflectionUtil.getFieldValue(owner, ownerColumn.getIdentifier());
        original.addAll(values);
      });
    }
  }

  public LinkedList<Runnable> getRunAfterImport() {
    return runAfterImport;
  }
}
