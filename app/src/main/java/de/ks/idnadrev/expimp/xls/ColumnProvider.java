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
import de.ks.idnadrev.expimp.EntityExportSource;
import de.ks.persistence.entity.AbstractPersistentObject;
import de.ks.persistence.entity.NamedPersistentObject;
import de.ks.reflection.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ColumnProvider {
  public List<XlsxColumn> getColumns(EntityExportSource<?> source) {
    Class<? extends AbstractPersistentObject> root = source.getRoot();
    return getColumnsByReflection(root);
  }

  @SuppressWarnings("unchecked")
  public List<XlsxColumn> getColumns(Class<?> clazz) {
    if (AbstractPersistentObject.class.isAssignableFrom(clazz)) {
      return getColumnsByReflection((Class<? extends AbstractPersistentObject>) clazz);
    } else {
      return Collections.emptyList();
    }
  }

  private List<XlsxColumn> getColumnsByReflection(Class<? extends AbstractPersistentObject> clazz) {
    List<Field> fields = ReflectionUtil.getAllFields(clazz, f -> !Modifier.isFinal(f.getModifiers()), f -> !Modifier.isStatic(f.getModifiers()));
    return fields.stream().sequential().filter(this::isMatchingField).map(f -> new ReflectionColumn(clazz, f)).collect(Collectors.toList());
  }

  private boolean isMatchingField(Field field) {
    boolean retval = field.getType().equals(String.class);
    retval = retval || Number.class.isAssignableFrom(Primitives.unwrap(field.getType()));
    retval = retval || field.getType().equals(LocalTime.class);
    retval = retval || field.getType().equals(LocalDate.class);
    retval = retval || field.getType().equals(LocalDateTime.class);
    retval = retval || Boolean.class.equals(Primitives.unwrap(field.getType()));
    retval = retval || AbstractPersistentObject.class.isAssignableFrom(field.getType());
    retval = retval || NamedPersistentObject.class.isAssignableFrom(field.getType());
    return retval;
  }
}
