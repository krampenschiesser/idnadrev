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

import de.ks.idnadrev.expimp.xls.sheet.ImportSheetHandler;
import de.ks.idnadrev.expimp.xls.sheet.ImportValue;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.entity.AbstractPersistentObject;
import de.ks.persistence.entity.NamedPersistentObject;
import de.ks.reflection.ReflectionUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SingleSheetImport implements Callable<Void> {
  private static final Logger log = LoggerFactory.getLogger(SingleSheetImport.class);

  protected final ColumnProvider columnProvider = new ColumnProvider();
  private final Class<?> clazz;
  private final InputStream sheetStream;
  private final EntityType<?> entityType;
  private final XSSFReader reader;

  protected final LinkedList<Runnable> runAfterImport = new LinkedList<>();

  public SingleSheetImport(Class<?> clazz, InputStream sheetStream, EntityType<?> entityType, XSSFReader reader) {
    this.clazz = clazz;
    this.sheetStream = sheetStream;
    this.entityType = entityType;
    this.reader = reader;
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
      log.error("Failed to parse sheet {} ", clazz.getName(), e);
      throw new RuntimeException(e);
    } finally {
      try {
        sheetStream.close();
      } catch (IOException e) {
        log.error("Could not close sheet stream {}", clazz.getName(), e);
        throw new RuntimeException(e);
      }
      return null;
    }
  }

  public void importEntity(List<ImportValue> importValues) {
    if (importValues.isEmpty()) {
      return;
    }

    Object instance = ReflectionUtil.newInstance(clazz);


    List<ToOneRelationAssignment> manadatoryRelations = getManadatoryRelations(importValues, instance);
    List<ToOneRelationAssignment> optionalRelations = getOptionalRelations(importValues, null);

    manadatoryRelations.forEach(r -> r.run());
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
    runAfterImport.addAll(optionalRelations);

    PersistentWork.persist(instance);
    log.debug("Persisted {}", instance);
  }

  private List<ToOneRelationAssignment> getManadatoryRelations(List<ImportValue> importValues, Object instance) {
    List<SingularAttribute<?, ?>> mandatoryRelations = entityType.getSingularAttributes().stream().filter(a -> a.isAssociation() && !a.isOptional()).collect(Collectors.toList());

    return mandatoryRelations.stream().map(r -> {
      Optional<ImportValue> found = importValues.stream().filter(v -> v.getColumnDef().getIdentifier().equals(r.getName())).findFirst();
      if (!found.isPresent()) {
        log.warn("Could not import {} no column definition for {}.{} found. Values: {}", clazz.getName(), r.getJavaType().getName(), r.getName(), importValues);
        return null;
      }
      ImportValue importValue = found.get();
      importValues.remove(importValue);
      return new ToOneRelationAssignment(() -> instance, r, importValue.getColumnDef(), importValue.getValue());
    }).collect(Collectors.toList());
  }

  private List<ToOneRelationAssignment> getOptionalRelations(List<ImportValue> importValues, Object identifier) {
    List<SingularAttribute<?, ?>> optionalRelations = entityType.getSingularAttributes().stream().filter(a -> a.isAssociation() && a.isOptional()).collect(Collectors.toList());

    return optionalRelations.stream().map(r -> {
      Optional<ImportValue> found = importValues.stream().filter(v -> v.getColumnDef().getIdentifier().equals(r.getName())).findFirst();
      if (!found.isPresent()) {
        return null;
      }
      ImportValue importValue = found.get();
      importValues.remove(importValue);
      return new ToOneRelationAssignment(identifier, r, importValue.getColumnDef(), importValue.getValue());
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
      @SuppressWarnings("unchecked") Object o = PersistentWork.byId((Class<? extends AbstractPersistentObject>) type, (Long) identifier);
      return o;
    }
    return null;
  }

  static class ToOneRelationAssignment implements Runnable {
    XlsxColumn ownerColumn;

    Supplier<Object> ownerResolver;
    Supplier<Object> relationResolver;

    ToOneRelationAssignment(Supplier<Object> ownerResolver, SingularAttribute<?, ?> relation, XlsxColumn ownerColumn, Object relationIdentifier) {
      this.ownerColumn = ownerColumn;
      this.ownerResolver = ownerResolver;
      relationResolver = () -> resolveEntity(relation.getJavaType(), relationIdentifier);
    }

    ToOneRelationAssignment(Object ownerIdentifier, SingularAttribute<?, ?> relation, XlsxColumn ownerColumn, Object relationIdentifier) {
      ownerResolver = () -> resolveEntity(relation.getDeclaringType().getJavaType(), ownerIdentifier);
      relationResolver = () -> resolveEntity(relation.getJavaType(), relationIdentifier);
      this.ownerColumn = ownerColumn;
    }

    @Override
    public void run() {
      PersistentWork.wrap(() -> {
        Object owner = ownerResolver.get();
        Object value = relationResolver.get();
        ownerColumn.setValue(owner, value);
      });
    }
  }

  public LinkedList<Runnable> getRunAfterImport() {
    return runAfterImport;
  }
}
