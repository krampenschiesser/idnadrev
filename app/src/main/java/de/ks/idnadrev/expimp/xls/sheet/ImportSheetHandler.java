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
package de.ks.idnadrev.expimp.xls.sheet;

import com.google.common.primitives.Primitives;
import de.ks.idnadrev.expimp.xls.ColumnProvider;
import de.ks.idnadrev.expimp.xls.XlsxColumn;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

public class ImportSheetHandler extends DefaultHandler {
  private static final Logger log = LoggerFactory.getLogger(ImportSheetHandler.class);

  public static final String CELL_TYPE_STRING = "s";
  public static final String CELL_TYPE_INLINESTRING = "inlineStr";
  public static final String CELL_TYPE_NUMBER = "n";

  private static final String INLINESTRING_ELEMENT = "is";
  private static final String VALUE_ELEMENT = "v";
  private static final String CELL_ELEMENT = "c";
  private static final String CELL_ATTRIBUTE_TYPE = "t";
  private static final String CELL_ATTRIBUTE_ID = "r";
  private static final String CELL_ATTRIBUTE_STYLE = "s";

  public static final int COLUMN_DEF_ROW = 1;

  private final Class<?> clazz;
  private final SharedStringsTable sharedStringsTable;
  private final ColumnProvider columnProvider;
  private final Consumer<List<ImportValue>> importCallback;

  private final List<XlsxColumn> columns;
  private ImportValueParser<?> nextValue;

  private CellId currentCell;
  private final Map<String, XlsxColumn> columnId2Converter = new HashMap<>();
  private final List<ImportValue> currentValues;
  private XlsxColumn currentColumnDef;

  public ImportSheetHandler(Class<?> class2Import, SharedStringsTable sharedStringsTable, ColumnProvider columnProvider, Consumer<List<ImportValue>> importCallback) {
    this.clazz = class2Import;
    this.sharedStringsTable = sharedStringsTable;
    this.columnProvider = columnProvider;
    this.importCallback = importCallback;
    columns = columnProvider.getColumns(clazz);
    currentValues = new ArrayList<>(columns.size());
  }

  public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
    try {
      if (name.equals(CELL_ELEMENT)) {
        CellId lastCell = currentCell;
        currentCell = CellId.from(attributes.getValue(CELL_ATTRIBUTE_ID));
        if (lastCell != null && lastCell.row < currentCell.row) {
          importCallback.accept(currentValues);
          currentValues.clear();
        }
        String cellType = attributes.getValue(CELL_ATTRIBUTE_TYPE);

        log.trace("Cell {} has type {}", attributes.getValue(CELL_ATTRIBUTE_ID), cellType);

        if (currentCell.row == COLUMN_DEF_ROW) {
          nextValue = getNextValueForDefinition(cellType);
        } else if (cellType != null) {
          currentColumnDef = columnId2Converter.get(currentCell.col);
          if (currentColumnDef != null) {
            nextValue = getColumnParser(currentColumnDef, cellType);
          } else {
            nextValue = null;
          }
        } else {
          nextValue = null;
        }
      } else if (name.equals(VALUE_ELEMENT) || name.equals(INLINESTRING_ELEMENT)) {
        if (nextValue != null) {
          nextValue.beginRecording();
        }
      }
    } catch (Exception e) {
      throw new SAXException(e);
    }
  }

  @Override
  public void endDocument() throws SAXException {
    try {
      importCallback.accept(currentValues);
      currentValues.clear();
    } catch (Exception e) {
      throw new SAXException(e);
    }
  }

  public void endElement(String uri, String localName, String name) throws SAXException {
    try {
      if (name.equals(VALUE_ELEMENT) || name.equals(INLINESTRING_ELEMENT)) {
        if (nextValue != null) {
          nextValue.endRecording();

          if (currentCell.row == COLUMN_DEF_ROW) {
            String columnName = (String) nextValue.getValue();
            Optional<XlsxColumn> column = columns.stream()//
                    .filter(c -> c.getIdentifier().toLowerCase(Locale.ENGLISH).equals(columnName.toLowerCase(Locale.ENGLISH)))//
                    .findFirst();
            if (column.isPresent()) {
              columnId2Converter.put(currentCell.col, column.get());
              log.debug("Found column definition {} for column {}, pos={}", column.get(), columnName, currentCell);
            } else {
              log.warn("Could not find any column definition for {}, pos={}", columnName, currentCell);
            }
          } else {
            Object value = nextValue.getValue();
            if (currentColumnDef == null) {
              throw new IllegalStateException("column def is null for cell" + currentCell);
            }
            currentValues.add(new ImportValue(currentColumnDef, value));
            log.trace("new value {} in cell {}", value, currentCell);
          }
        }
      }
    } catch (Exception e) {
      throw new SAXException(e);
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    try {
      if (nextValue != null) {
        String val = String.valueOf(Arrays.copyOfRange(ch, start, start + length));
        nextValue.append(val);
      }
    } catch (Exception e) {
      throw new SAXException(e);
    }
  }

  private ImportValueParser<?> getNextValueForDefinition(String cellType) {
    if (CELL_TYPE_INLINESTRING.equals(cellType)) {
      return new InlineStringValueParser();
    }
    if (CELL_TYPE_STRING.equals(cellType)) {
      return new SharedStringValueParser(sharedStringsTable);
    }
    return null;
  }

  private ImportValueParser<?> getColumnParser(XlsxColumn columnDef, String cellType) {
    Class<?> fieldType = columnDef.getFieldType();

    if (String.class.equals(fieldType)) {
      if (cellType.equals(CELL_TYPE_INLINESTRING)) {
        return new InlineStringValueParser();
      } else if (cellType.equals(CELL_TYPE_STRING)) {
        return new SharedStringValueParser(sharedStringsTable);
      }
    }

    if (LocalDateTime.class.equals(fieldType)) {
      return new LocalDateTimeValueParser();
    }
    if (LocalDate.class.equals(fieldType)) {
      return new LocalDateValueParser();
    }
    if (Duration.class.equals(fieldType)) {
      return new DurationValueParser();
    }


    Class<?> unwrap = Primitives.unwrap(fieldType);
    if (Double.class.isAssignableFrom(unwrap) || Float.class.isAssignableFrom(unwrap)) {
      return new DoubleValueParser();
    }
    if (Long.class.isAssignableFrom(unwrap) || Integer.class.isAssignableFrom(unwrap)) {
      return new LongValueParser();
    }
    log.warn("No value parser for celltype {} and coumndef {}", cellType, columnDef);
    return null;
  }

  //Print exceptions

  @Override
  public void warning(SAXParseException e) throws SAXException {
    log.error("Could not parse {}", currentCell, e);
  }

  @Override
  public void error(SAXParseException e) throws SAXException {
    log.error("Could not parse {}", currentCell, e);
  }

  @Override
  public void fatalError(SAXParseException e) throws SAXException {
    log.error("Could not parse {}", currentCell, e);
  }

}
