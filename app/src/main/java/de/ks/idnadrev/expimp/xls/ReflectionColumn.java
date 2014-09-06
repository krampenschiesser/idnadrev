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
import de.ks.persistence.entity.AbstractPersistentObject;
import de.ks.persistence.entity.NamedPersistentObject;
import de.ks.reflection.ReflectionUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReflectionColumn implements XlsxColumn {
  private static final Logger log = LoggerFactory.getLogger(ReflectionColumn.class);
  protected final int cellType;
  protected Class<?> root;
  protected Field field;
  protected MethodHandle getter;
  protected Class<?> fieldType;

  public ReflectionColumn(Class<?> root, Field field) {
    this.field = field;
    field.setAccessible(true);
    this.root = root;

    try {
      getter = MethodHandles.lookup().unreflectGetter(field);
    } catch (IllegalAccessException e) {
      log.error("Could not get handle for field {}", field, e);
      getter = null;
    }

    fieldType = field.getType();
    cellType = getCellType(fieldType);
  }

  private int getCellType(Class<?> type) {
    if (Number.class.isAssignableFrom(Primitives.unwrap(type))) {
      return Cell.CELL_TYPE_NUMERIC;
    } else if (Boolean.class.isAssignableFrom(Primitives.unwrap(type))) {
      return Cell.CELL_TYPE_BOOLEAN;
    } else {
      return Cell.CELL_TYPE_STRING;
    }
  }

  @Override
  public Class<?> getRoot() {
    return root;
  }

  @Override
  public String getIdentifier() {
    return field.getName();
  }

  @Override
  public Object getValue(Object object) {
    Object valueInternal = getValueInternal(object);
    if (valueInternal instanceof NamedPersistentObject) {
      return ((NamedPersistentObject) valueInternal).getName();
    } else if (valueInternal instanceof AbstractPersistentObject) {
      return ((AbstractPersistentObject) valueInternal).getId();
    } else {
      return valueInternal;
    }
  }

  public Object getValueInternal(Object object) {
    if (getter != null) {
      try {
        return getter.invoke(object);
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    } else {
      return ReflectionUtil.getFieldValue(object, field);
    }
  }

  @Override
  public int getCellType() {
    return cellType;
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

  @Override
  public CellStyle getCellStyle(SXSSFWorkbook workbook) {
    CreationHelper creationHelper = workbook.getCreationHelper();
    if (LocalDateTime.class.isAssignableFrom(fieldType)) {
      CellStyle cellStyle = workbook.createCellStyle();
      cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy/mm/dd hh:mm:ss"));
      return cellStyle;
    } else if (LocalDate.class.isAssignableFrom(fieldType)) {
      CellStyle cellStyle = workbook.createCellStyle();
      cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy/mm/dd"));
      return cellStyle;
    }
    return null;
  }
}
