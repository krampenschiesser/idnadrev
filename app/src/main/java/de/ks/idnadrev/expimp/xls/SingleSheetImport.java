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
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;

public class SingleSheetImport implements Callable<Void> {
  private static final Logger log = LoggerFactory.getLogger(SingleSheetImport.class);

  protected final ColumnProvider columnProvider = new ColumnProvider();
  private final Class<?> clazz;
  private final InputStream sheetStream;
  private final EntityType<?> entityType;
  private final XSSFReader reader;

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
    importValues.forEach(v -> {
      XlsxColumn columnDef = v.getColumnDef();
      Object value = v.getValue();
      columnDef.setValue(instance, value);
    });
    PersistentWork.persist(instance);
    log.debug("Persisted {}", instance);
  }
}
