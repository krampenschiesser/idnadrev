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
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

public class XlsxImporter {
  private static final Logger log = LoggerFactory.getLogger(XlsxImporter.class);

  protected final ColumnProvider columnProvider = new ColumnProvider();
  private final ExecutorService executorService;

  public XlsxImporter(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public void importFromFile(File file) {
    checkFile(file);
    OPCPackage pkg = openPackage(file);
    try {
      XSSFReader reader = new XSSFReader(pkg);
      SharedStringsTable sharedStringsTable = reader.getSharedStringsTable();//used by ms office to store all string values
      log.info("Importing from {}", file);

      XSSFReader.SheetIterator iterator = (XSSFReader.SheetIterator) reader.getSheetsData();
      while (iterator.hasNext()) {
        InputStream sheetStream = iterator.next();

        String sheetName = iterator.getSheetName();
        Class<?> class2Import = null;
        try {
          class2Import = getClass().getClassLoader().loadClass(sheetName);
        } catch (ClassNotFoundException e) {
          log.info("Could not load class to import {} will skip sheet.", sheetName);
          continue;
        }

        XMLReader parser = XMLReaderFactory.createXMLReader();
        ImportSheetHandler importSheetHandler = new ImportSheetHandler(class2Import, sharedStringsTable, columnProvider, new ImportCallback(class2Import));
        parser.setContentHandler(importSheetHandler);


        InputSource inputSource = new InputSource(sheetStream);
        executorService.submit(() -> {
          try {
            parser.parse(inputSource);
          } catch (SAXException | IOException e) {
            log.error("Failed to parse sheet {} ", sheetName, e);
            throw new RuntimeException(e);
          } finally {
            try {
              sheetStream.close();
            } catch (IOException e) {
              log.error("Could not clsoe sheet stream {}", sheetName, e);
              throw new RuntimeException(e);
            }
          }
        });
      }

    } catch (OpenXML4JException | IOException e) {
      log.error("Could not read {}", file, e);
      throw new RuntimeException(e);
    } catch (SAXException e) {
      log.error("Could not create sax parser", e);
    } finally {
      try {
        pkg.close();
      } catch (IOException e) {
        log.error("Could not close package {}", pkg, e);
      }
    }
  }

  public OPCPackage openPackage(File file) {
    OPCPackage pkg = null;
    try {
      pkg = OPCPackage.open(file);
    } catch (InvalidFormatException e) {
      log.error("Could not create opc package for file {}", file, e);
      throw new RuntimeException(e);
    }
    return pkg;
  }

  private void checkFile(File file) {
    if (file == null) {
      throw new NullPointerException("File must not be null");
    }
    if (!file.exists()) {
      throw new IllegalArgumentException("File " + file + " has to exist");
    }
  }
}
