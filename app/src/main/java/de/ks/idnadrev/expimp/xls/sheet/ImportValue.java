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

import de.ks.idnadrev.expimp.xls.XlsxColumn;

public class ImportValue {
  protected final XlsxColumn columnDef;
  protected final Object value;

  public ImportValue(XlsxColumn columnDef, Object value) {
    this.columnDef = columnDef;
    this.value = value;
  }

  public XlsxColumn getColumnDef() {
    return columnDef;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ImportValue{");
    sb.append("columnDef=").append(columnDef);
    sb.append(", value=").append(value);
    sb.append('}');
    return sb.toString();
  }
}
