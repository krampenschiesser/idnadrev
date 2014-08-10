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
package de.ks.binding;

import de.ks.reflection.PropertyPath;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class Binding {
  private static final Logger log = LoggerFactory.getLogger(Binding.class);
  private final Map<PropertyPath, Property<?>> properties = new HashMap<>();
  private final Map<Property<?>, Pair<Function, Function>> converters = new HashMap<>();

  public void bindChangedModel(ObservableValue<?> observable, Object oldValue, Object newValue) {
    log.debug("Binding changed model old={}, new={}", oldValue, newValue);
    if (newValue != null) {
      applyModelToProperties(newValue);
    } else {
      resetProperties();
    }
  }

  protected void resetProperties() {
    properties.entrySet().forEach(entry -> {
      @SuppressWarnings("unchecked") Property<Object> property = (Property<Object>) entry.getValue();
      if (!property.isBound()) {
        property.setValue(null);
      }
    });
  }

  @SuppressWarnings("unchecked")
  protected void applyModelToProperties(Object model) {
    properties.entrySet().forEach(entry -> {
      Object value = entry.getKey().getValue(model);
      @SuppressWarnings("unchecked") Property<Object> property = (Property<Object>) entry.getValue();
      Pair<Function, Function> converter = converters.get(property);
      if (converter != null && value != null) {
        value = converter.getKey().apply(value);
      }
      if (!property.isBound()) {
        property.setValue(value);
      }
    });
  }

  public void applyControllerContent(Object model) {
    properties.entrySet().forEach(entry -> {
      @SuppressWarnings("unchecked") Property<Object> property = (Property<Object>) entry.getValue();
      Object value = property.getValue();
      log.debug("Setting property {} to {}", property, value);
      entry.getKey().setValue(model, value);
    });
  }

  public <T extends Object> StringProperty getStringProperty(Class<T> clazz, Function<T, String> propertyResolution) {
    return addProperty(clazz, propertyResolution, SimpleStringProperty::new, null, null);
  }

  public <T extends Object> BooleanProperty getBooleanProperty(Class<T> clazz, Function<T, Boolean> propertyResolution) {
    return addProperty(clazz, propertyResolution, SimpleBooleanProperty::new, null, null);
  }

  public <T extends Object> IntegerProperty getIntegerProperty(Class<T> clazz, Function<T, Integer> propertyResolution) {
    return addProperty(clazz, propertyResolution, SimpleIntegerProperty::new, null, null);
  }

  public <T extends Object> LongProperty getLongProperty(Class<T> clazz, Function<T, Long> propertyResolution) {
    return addProperty(clazz, propertyResolution, SimpleLongProperty::new, null, null);
  }

  public <T extends Object> FloatProperty getFloatProperty(Class<T> clazz, Function<T, Float> propertyResolution) {
    return addProperty(clazz, propertyResolution, SimpleFloatProperty::new, null, null);
  }

  public <T extends Object> DoubleProperty getDoubleProperty(Class<T> clazz, Function<T, Double> propertyResolution) {
    return addProperty(clazz, propertyResolution, SimpleDoubleProperty::new, null, null);
  }

  public <T extends Object, V> ObjectProperty<V> getObjectProperty(Class<T> clazz, Function<T, V> propertyResolution) {
    return addProperty(clazz, propertyResolution, SimpleObjectProperty::new, null, null);
  }

  public <T extends Object, V> ListProperty<V> getListProperty(Class<T> clazz, Function<T, List<V>> propertyResolution) {
    return addProperty(clazz, propertyResolution, SimpleListProperty::new, FXCollections::observableList, (ObservableList<V> l) -> l);
  }

  public <T extends Object, V> SetProperty<V> getSetProperty(Class<T> clazz, Function<T, Set<V>> propertyResolution) {
    return addProperty(clazz, propertyResolution, SimpleSetProperty::new, FXCollections::observableSet, (ObservableSet<V> l) -> l);
  }

  public <T extends Object, K, V> MapProperty<K, V> getMapProperty(Class<T> clazz, Function<T, Map<K, V>> propertyResolution) {
    return addProperty(clazz, propertyResolution, SimpleMapProperty::new, FXCollections::observableMap, (ObservableMap<K, V> m) -> m);
  }

  @SuppressWarnings("unchecked")
  protected <T extends Property, V, K, O> T addProperty(Class<V> clazz, Function<V, K> function, Supplier<T> supplier, Function<K, O> model2Controller, Function<O, K> controller2Model) {
    PropertyPath path = PropertyPath.ofTypeSafe(clazz, function);
    if (!properties.containsKey(path)) {
      T value = supplier.get();
      properties.put(path, value);
      if (model2Controller != null && controller2Model != null) {
        converters.put(value, Pair.of(model2Controller, controller2Model));
      } else if (model2Controller != null || controller2Model != null) {
        throw new IllegalArgumentException("Please specify both converters");
      }
    }
    return (T) properties.get(path);
  }
}
