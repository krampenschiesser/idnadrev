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

import de.ks.persistence.entity.AbstractPersistentObject;
import de.ks.persistence.entity.NamedPersistentObject;
import de.ks.reflection.ReflectionUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.persistence.metamodel.PluralAttribute;
import java.util.Collection;

public class ToManyColumn implements XlsxColumn {

  public static final String SEPARATOR = "|";
  public static final String SEPARATOR_REPLACEMENT = "&#124;";

  private final PluralAttribute attribute;

  public ToManyColumn(PluralAttribute attribute) {
    this.attribute = attribute;
  }

  @Override
  public Class<?> getRoot() {
    return attribute.getDeclaringType().getJavaType();
  }

  @Override
  public String getIdentifier() {
    return attribute.getName();
  }

  @Override
  public Object getValue(Object object) {

    StringBuilder builder = new StringBuilder();
    if (Collection.class.isAssignableFrom(attribute.getJavaType())) {

      Collection collection = (Collection) ReflectionUtil.getFieldValue(object, attribute.getName());
      for (java.util.Iterator iterator = collection.iterator(); iterator.hasNext(); ) {
        Object next = iterator.next();
        if (NamedPersistentObject.class.isAssignableFrom(next.getClass())) {
          String name = (String) ReflectionUtil.getFieldValue(next, "name");
          name = StringUtils.replace(name, SEPARATOR, SEPARATOR_REPLACEMENT);
          builder.append(name).append(SEPARATOR);
        } else if (AbstractPersistentObject.class.isAssignableFrom(next.getClass())) {
          Long id = (Long) ReflectionUtil.getFieldValue(next, "id");
          builder.append(id).append(SEPARATOR);
        }
      }
      if (builder.length() == 0) {
        return null;
      } else {
        return builder.substring(0, builder.length() - 1);
      }
    }
    throw new IllegalArgumentException("Cannot resolve to many relation " + attribute.getDeclaringType().getJavaType().getName() + "." + attribute.getName());
  }

  @Override
  public int getCellType() {
    return Cell.CELL_TYPE_STRING;
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

  @Override
  public CellStyle getCellStyle(SXSSFWorkbook workbook) {
    return null;
  }

  @Override
  public Class<?> getFieldType() {
    return String.class;
  }

  @Override
  public void setValue(Object instance, Object value) {

  }
}
