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

import com.google.common.primitives.Primitives;
import com.google.common.util.concurrent.MoreExecutors;
import de.ks.idnadrev.expimp.DependencyGraph;
import de.ks.idnadrev.expimp.EntityExportSource;
import de.ks.idnadrev.expimp.Exporter;
import de.ks.persistence.entity.AbstractPersistentObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class XlsxExporter implements Exporter {
  private static final Logger log = LoggerFactory.getLogger(XlsxExporter.class);
  protected final SXSSFWorkbook workbook;
  protected ExecutorService executorService;
  protected final ColumnProvider provider;

  public XlsxExporter() {
    this(MoreExecutors.newDirectExecutorService());
  }

  public XlsxExporter(ExecutorService executorService) {
    workbook = new SXSSFWorkbook();
    workbook.setCompressTempFiles(true);
    this.executorService = executorService;
    provider = new ColumnProvider(CDI.current().select(DependencyGraph.class).get());
  }

  public void setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
  }

  @Override
  public void export(File file, EntityExportSource<?>... sources) {
    List<EntityExportSource<?>> exportSources = Arrays.asList(sources);
    export(file, exportSources);
  }

  @Override
  public void export(File file, List<EntityExportSource<?>> sources) {
    if (file.exists()) {
      file.delete();
    }
    List<Future<?>> futures = sources.stream()//
            .filter(s -> s.getIds().size() > 0)//
            .map(s -> {
              return executorService.submit(() -> {
                String identifier = s.getIdentifier();
                log.info("Exporting {} to {}", identifier, file);
                Sheet sheet = workbook.createSheet(identifier);
                exportSource(sheet, s);
              });
            }).collect(Collectors.toList());

    join(futures);

    try (FileOutputStream out = new FileOutputStream(file)) {
      workbook.write(out);
    } catch (IOException e) {
      log.error("Could not write file {}", file, e);
    } finally {
      workbook.dispose();
    }
  }

  private void join(List<Future<?>> export) {
    export.forEach(sheet -> {
      try {
        sheet.get();
      } catch (InterruptedException e) {
        //
      } catch (ExecutionException e) {
        log.error("Failed to export", e);
        throw new RuntimeException(e.getCause());
      }
    });
  }

  protected void exportSource(Sheet sheet, EntityExportSource<?> source) {
    List<XlsxColumn> columns = getColumnDefinitions(source);
    createTitle(sheet, columns);

    int rowId = 1;
    for (AbstractPersistentObject object : source) {
      Row row = sheet.createRow(rowId);
      for (int columnId = 0; columnId < columns.size(); columnId++) {
        XlsxColumn column = columns.get(columnId);
        Object value = column.getValue(object);
        if (value == null) {
          row.createCell(columnId, Cell.CELL_TYPE_BLANK);
        } else {
          Cell cell = row.createCell(columnId, column.getCellType());
          cell.setCellStyle(column.getCellStyle(workbook));
          setCellValue(sheet.getWorkbook().getCreationHelper(), cell, value);
        }
      }
      rowId++;
    }
    for (int columnId = 0; columnId < columns.size(); columnId++) {
      try {
        sheet.autoSizeColumn(columnId);
      } catch (NullPointerException e) {
        //
      }
    }
  }

  private void createTitle(Sheet sheet, List<XlsxColumn> columns) {
    Row title = sheet.createRow(0);
    for (int columnId = 0; columnId < columns.size(); columnId++) {
      XlsxColumn column = columns.get(columnId);
      Cell titleCell = title.createCell(columnId, Cell.CELL_TYPE_STRING);

      RichTextString richTextString = workbook.getCreationHelper().createRichTextString(column.getIdentifier());
      titleCell.setCellValue(richTextString);

      CellStyle cellStyle = getTitleStyle();
      titleCell.setCellStyle(cellStyle);
    }
  }

  private CellStyle getTitleStyle() {
    CellStyle cellStyle = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setFontHeightInPoints((short) 12);
    font.setBoldweight(Font.BOLDWEIGHT_BOLD);
    cellStyle.setFont(font);
    return cellStyle;
  }

  protected void setCellValue(CreationHelper creationHelper, Cell cell, Object value) {
    if (value instanceof Number) {
      double cellValue = ((Number) value).doubleValue();
      cell.setCellValue(cellValue);
    } else if (Number.class.isAssignableFrom(Primitives.wrap(value.getClass()))) {
      double cellValue = (double) value;
      cell.setCellValue(cellValue);
    } else if (value instanceof String) {
      RichTextString richTextString = creationHelper.createRichTextString((String) value);
      cell.setCellValue(richTextString);
    } else if (value instanceof LocalDateTime) {
      long time = Timestamp.valueOf(((LocalDateTime) value)).getTime();
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(time);
      cell.setCellValue(cal);
    }
  }

  protected List<XlsxColumn> getColumnDefinitions(EntityExportSource<?> source) {
    List<XlsxColumn> collect = provider.getColumns(source).stream()//
            .filter(c -> !source.getConfig().getIgnoredFields().contains(c.getIdentifier()))//
            .collect(Collectors.toList());
    return collect;
  }
}
