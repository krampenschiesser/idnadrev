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
import de.ks.persistence.entity.AbstractPersistentObject;
import de.ks.persistence.entity.NamedPersistentObject;
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

    String name;
    if (NamedPersistentObject.class.isAssignableFrom(clazz)) {
      Optional<ImportValue> first = importValues.stream().filter(v -> v.getColumnDef().getIdentifier().equals("name")).findFirst();
      name = (String) first.get().getValue();
    } else {
      name = null;
    }


    PersistentWork.run(em -> {
      Object instance;
      boolean keepExisting = importCfg.isKeepExisting();
      boolean exists;

      if (name != null) {
        @SuppressWarnings("unchecked") Class<? extends NamedPersistentObject> npoClass = (Class<? extends NamedPersistentObject>) clazz;
        NamedPersistentObject loaded = PersistentWork.forName(npoClass, name);
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
        result.success("Ignored " + clazz.getName() + " with identifier " + name, importValues.get(0).getCellId());
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
            return resolveEntity(clazz, identifier);
          };
        });


        if (!exists) {
          em.persist(instance);
        }
        result.success(instance.toString(), importValues.get(0).getCellId());
        toManyRelations.forEach(r -> {
          r.ownerResolver = () -> {
            Object identifier = getIdentifier(instance);
            return resolveEntity(clazz, identifier);
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

      Consumer<Object> resultWriter = o -> result.warn("could not find association of '" + o + "' via '" + r.getName() + "' to '" + importValue.getValue() + "'", importValue.getCellId());
      return new ToOneRelationAssignment(resultWriter, () -> instance, r, importValue.getColumnDef(), importValue.getValue());
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
      return new ToOneRelationAssignment(resultWriter, null, r, importValue.getColumnDef(), importValue.getValue());
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
      return new ToManyRelationAssignment(importValue.getColumnDef(), r, (String) importValue.getValue());
    }).filter(r -> r != null).collect(Collectors.toList());
  }

  static Object getIdentifier(Object instance) {
    if (NamedPersistentObject.class.isAssignableFrom(instance.getClass())) {
      return ReflectionUtil.getFieldValue(instance, "name");
    } else if (AbstractPersistentObject.class.isAssignableFrom(instance.getClass())) {
      return ReflectionUtil.getFieldValue(instance, "id");
    }
    return null;
  }

  static Object resolveEntity(Class<?> type, Object identifier) {
    if (NamedPersistentObject.class.isAssignableFrom(type)) {
      @SuppressWarnings("unchecked") Object o = PersistentWork.forName((Class<? extends NamedPersistentObject>) type, (String) identifier);
      return o;
    } else if (AbstractPersistentObject.class.isAssignableFrom(type)) {
      Long id;
      if (identifier instanceof String) {
        id = Long.parseLong((String) identifier);
      } else {
        id = (Long) identifier;
      }
      @SuppressWarnings("unchecked") Object o = PersistentWork.byId((Class<? extends AbstractPersistentObject>) type, id);
      return o;
    }
    return null;
  }

  static List<Object> resolveToManyRelation(Class<?> type, List<String> singleIdentifiers) {
    LinkedList<Object> retval = new LinkedList<>();
    for (String identifier : singleIdentifiers) {
      Object entity = resolveEntity(type, identifier);
      retval.add(entity);
    }
    return retval;
  }

  static class ToOneRelationAssignment implements Runnable {
    private final Consumer<Object> resultWriter;
    XlsxColumn ownerColumn;

    Supplier<Object> ownerResolver;
    Supplier<Object> relationResolver;

    ToOneRelationAssignment(Consumer<Object> resultWriter, Supplier<Object> ownerResolver, SingularAttribute<?, ?> relation, XlsxColumn ownerColumn, Object relationIdentifier) {
      this.resultWriter = resultWriter;
      this.ownerColumn = ownerColumn;
      this.ownerResolver = ownerResolver;
      relationResolver = () -> resolveEntity(relation.getJavaType(), relationIdentifier);
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

    ToManyRelationAssignment(XlsxColumn ownerColumn, PluralAttribute relation, String relationString) {
      this.ownerColumn = ownerColumn;

      String[] split = StringUtils.split(relationString, "|");
      ArrayList<String> singleIdentifiers = new ArrayList<>(split.length);
      for (String string : split) {
        String id = StringUtils.replace(string, ToManyColumn.SEPARATOR, ToManyColumn.SEPARATOR_REPLACEMENT);
        singleIdentifiers.add(id);
      }
      relationResolver = () -> resolveToManyRelation(relation.getElementType().getJavaType(), singleIdentifiers);
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
