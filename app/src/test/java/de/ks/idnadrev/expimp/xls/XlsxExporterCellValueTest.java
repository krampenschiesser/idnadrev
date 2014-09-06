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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.RichTextString;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Calendar;

public class XlsxExporterCellValueTest {

  @Test
  public void testSetNumericCellValue() {
    XlsxExporter exporter = new XlsxExporter(null);

    Cell cell = Mockito.mock(Cell.class);

    exporter.setCellValue(null, cell, Long.valueOf(42));
    Mockito.verify(cell).setCellValue(42D);

    Mockito.reset(cell);
    exporter.setCellValue(null, cell, (byte) 3);
    Mockito.verify(cell).setCellValue(3D);

    Mockito.reset(cell);
    exporter.setCellValue(null, cell, Long.MAX_VALUE);
    Mockito.verify(cell).setCellValue((double) Long.MAX_VALUE);
  }

  @Test
  public void testString() throws Exception {
    XlsxExporter exporter = new XlsxExporter(null);

    Cell cell = Mockito.mock(Cell.class);
    CreationHelper helper = Mockito.mock(CreationHelper.class);
    RichTextString richTextString = Mockito.mock(RichTextString.class);

    Mockito.when(helper.createRichTextString("Hello World")).thenReturn(richTextString);

    exporter.setCellValue(helper, cell, "Hello World");
    Mockito.verify(cell).setCellValue(richTextString);
    Mockito.verify(helper).createRichTextString("Hello World");
  }

  @Test
  public void testLocalDateTime() throws Exception {
    XlsxExporter exporter = new XlsxExporter(null);
    LocalDateTime localDateTime = LocalDateTime.of(2014, 03, 06, 14, 42);

    Cell cell = Mockito.mock(Cell.class);
    exporter.setCellValue(null, cell, localDateTime);

    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(Timestamp.valueOf(localDateTime).getTime());
    Mockito.verify(cell).setCellValue(cal);
  }
}
