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

import com.google.common.util.concurrent.MoreExecutors;
import de.ks.idnadrev.expimp.DependencyGraph;
import de.ks.idnadrev.expimp.xls.result.XlsxImportResultCollector;
import de.ks.idnadrev.expimp.xls.result.XlsxImportSheetResult;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.metamodel.EntityType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class XlsxImporter {
  private static final Logger log = LoggerFactory.getLogger(XlsxImporter.class);

  private XlsxImportResultCollector resultCollector;
  private final ExecutorService executorService;
  private DependencyGraph dependencyGraph;
  protected boolean throwOnError = false;

  public XlsxImporter() {
    this(MoreExecutors.sameThreadExecutor());
  }

  public XlsxImporter(ExecutorService executorService) {
    this.executorService = executorService;
    dependencyGraph = CDI.current().select(DependencyGraph.class).get();
  }

  public XlsxImportResultCollector importFromFile(File file) {
    resultCollector = new XlsxImportResultCollector();
    checkFile(file);
    OPCPackage pkg = openPackage(file);
    try {
      XSSFReader reader = new XSSFReader(pkg);
      SharedStringsTable sharedStringsTable = reader.getSharedStringsTable();//used by ms office to store all string values
      log.info("Importing from {}", file);

      Map<Integer, Collection<SingleSheetImport>> importStages = new HashMap<>();

      XSSFReader.SheetIterator iterator = (XSSFReader.SheetIterator) reader.getSheetsData();
      while (iterator.hasNext()) {
        InputStream sheetStream = iterator.next();

        String sheetName = iterator.getSheetName();
        final XlsxImportSheetResult result = resultCollector.getSheetResult(sheetName);

        Class<?> class2Import = null;
        try {
          class2Import = getClass().getClassLoader().loadClass(sheetName);
        } catch (ClassNotFoundException e) {
          log.info("Could not load class to import {} will skip sheet.", sheetName);
          result.generalError("Could not load class to import " + sheetName + " will skip sheet.", e);
          continue;
        }

        if (class2Import != null) {
          int stage = dependencyGraph.getStage(class2Import);
          EntityType<?> entityType = dependencyGraph.getEntityType(class2Import);
          importStages.putIfAbsent(stage, new LinkedList<SingleSheetImport>());
          importStages.get(stage).add(new SingleSheetImport(class2Import, sheetStream, dependencyGraph, reader, result));
        }
      }

      importStages.entrySet().forEach(stage -> {
        try {
          executorService.invokeAll(stage.getValue());

          List<List<Future<?>>> collect = stage.getValue().stream()//
                  .map(sheet -> sheet.getRunAfterImport().stream().map((Runnable r) -> executorService.submit(r)).collect(Collectors.<Future<?>>toList()))//
                  .collect(Collectors.<List<Future<?>>>toList());

          for (List<Future<?>> futureList : collect) {
            futureList.forEach(future -> {
              try {
                future.get();
              } catch (ExecutionException e) {
                if (throwOnError) {
                  log.error("Could not run after sheet ", e);
                  throw new RuntimeException(e);
                }
              } catch (InterruptedException e) {
                //
              }
            });
          }
        } catch (InterruptedException e1) {
          //
        }
      });

    } catch (OpenXML4JException | IOException e) {
      resultCollector.generalError("Could not read " + file, e);
      if (throwOnError) {
        log.error("Could not read {}", file, e);
        throw new RuntimeException(e);
      }
    } finally {
      try {
        pkg.close();
      } catch (IOException e) {
        resultCollector.generalError("Could not close package " + pkg, e);
        if (throwOnError) {
          log.error("Could not close package {}", pkg, e);
        }
      }
    }
    return resultCollector;
  }

  protected OPCPackage openPackage(File file) {
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

  public boolean isThrowOnError() {
    return throwOnError;
  }

  public void setThrowOnError(boolean throwOnError) {
    this.throwOnError = throwOnError;
  }
}
