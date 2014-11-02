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
package de.ks.idnadrev.information.chart;

import javafx.beans.property.SimpleStringProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ChartRow {
  protected SimpleStringProperty category = new SimpleStringProperty();

  protected Map<Integer, SimpleStringProperty> values = new HashMap<>();

  public SimpleStringProperty getValue(int columnId) {
    values.putIfAbsent(columnId, new SimpleStringProperty());
    return values.get(columnId);
  }

  public void setValue(int columnId, String newValue) {
    SimpleStringProperty retval = values.putIfAbsent(columnId, new SimpleStringProperty(newValue));
    if (retval != null) {
      retval.set(newValue);
    }
  }

  public void setValue(int column, Double value) {
    setValue(column, String.valueOf(value));
  }

  public SimpleStringProperty getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category.set(category);
  }

  public boolean isEmpty() {
    boolean empty = getCategory().getValueSafe().isEmpty();
    Optional<Boolean> emptyValues = values.values().stream().map(s -> s == null || s.getValueSafe().isEmpty()).reduce((o1, o2) -> o1 && o2);
    if (emptyValues.isPresent()) {
      return empty && emptyValues.get();
    } else {
      return empty;
    }
  }

}
