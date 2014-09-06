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

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

public class ImportSheetHandler extends DefaultHandler {
  private static final Logger log = LoggerFactory.getLogger(ImportSheetHandler.class);

  private final SharedStringsTable sharedStringsTable;

  private static final String CELL_TYPE_STRING = "s";
  private static final String CELL_TYPE_INLINESTRING = "inlineStr";
  private static final String CELL_TYPE_NUMBER = "n";

  private static final String CELL_STYLE = "s";

  private static final String VALUE_ELEMENT = "v";//elelemt value
  private static final String CELL_ELEMENT = "c";//elelemt cell
  private static final String CELL_ATTRIBUTE_TYPE = "t";//attribute of cell element
  private static final String CELL_ATTRIBUTE_ID = "r";//attribute of cell element
  private NextValue<?> nextValue;

  private boolean columnDefsDiscovered;

  public ImportSheetHandler(SharedStringsTable sharedStringsTable) {
    this.sharedStringsTable = sharedStringsTable;
  }

  public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
    if (name.equals(CELL_ELEMENT)) {

      String cellId = attributes.getValue(CELL_ATTRIBUTE_ID);
      String cellType = attributes.getValue(CELL_ATTRIBUTE_TYPE);

      log.trace("Cell {} has type {}", cellId, cellType);

      nextValue = getNextValueForType(cellType);
    } else if (name.equals(VALUE_ELEMENT)) {
      if (nextValue != null) {
        nextValue.beginRecording();
      }
    }
  }

  public void endElement(String uri, String localName, String name) throws SAXException {
    if (name.equals(VALUE_ELEMENT)) {
      if (nextValue != null) {
        nextValue.endRecording();
        log.info("End of elemenet {} Got value {} from {}", name, nextValue.getValue(), nextValue.getClass().getSimpleName());
      }
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    if (nextValue != null) {
      String val = String.valueOf(Arrays.copyOfRange(ch, start, start + length));
      nextValue.append(val);
    }
  }

  private NextValue<?> getNextValueForType(String cellType) {
    if (CELL_TYPE_INLINESTRING.equals(cellType)) {
      return new InlineStringValue();
    }
    if (CELL_TYPE_STRING.equals(cellType)) {
      return new SharedStringValue(sharedStringsTable);
    }
    return null;
  }

  static abstract class NextValue<V> {
    protected final StringBuilder builder = new StringBuilder();
    protected boolean recording = false;

    public void append(String value) {
      if (recording) {
        builder.append(value);
      }
    }

    public void endRecording() {
      recording = false;
    }

    public void beginRecording() {
      recording = true;
    }

    public abstract V getValue();
  }

  static class InlineStringValue extends NextValue<String> {
    @Override
    public String getValue() {
      return new XSSFRichTextString(builder.toString()).getString();
    }
  }

  static class SharedStringValue extends NextValue<String> {
    private final SharedStringsTable table;

    public SharedStringValue(SharedStringsTable table) {
      this.table = table;
    }

    @Override
    public String getValue() {
      int tableIndex = Integer.parseInt(builder.toString());
      return new XSSFRichTextString(table.getEntryAt(tableIndex)).getString();
    }
  }

  static class LDTValue extends NextValue<LocalDateTime> {
    @Override
    public LocalDateTime getValue() {
      Double dVal = Double.valueOf(builder.toString());
      Date date = DateUtil.getJavaDate(dVal);
      return new Timestamp(date.getTime()).toLocalDateTime();
    }
  }

  static class LDValue extends NextValue<LocalDate> {
    @Override
    public LocalDate getValue() {
      Double dVal = Double.valueOf(builder.toString());
      Date date = DateUtil.getJavaDate(dVal);
      return new Timestamp(date.getTime()).toLocalDateTime().toLocalDate();
    }
  }

  static class DurationValue extends NextValue<Duration> {
    @Override
    public Duration getValue() {
      Long nanos = Long.valueOf(builder.toString());
      return Duration.ofNanos(nanos);
    }
  }
}
