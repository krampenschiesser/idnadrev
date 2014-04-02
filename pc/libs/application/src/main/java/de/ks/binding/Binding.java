/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
 *
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

import com.google.common.primitives.Primitives;
import de.ks.javafx.converter.LastValueConverter;
import de.ks.reflection.ReflectionUtil;
import javafx.beans.property.Property;
import javafx.beans.property.adapter.*;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TextInputControl;
import javafx.util.StringConverter;
import javafx.util.converter.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

public class Binding {
  private static final Logger log = LoggerFactory.getLogger(Binding.class);
  private final Map<String, Pair<Class<?>, Node>> propertyCandidates = new HashMap<>();
  private final Map<String, JavaBeanProperty<?>> properties = new HashMap<>();
  private final Set<Pair<JavaBeanProperty<?>, Property<?>>> bindings = new HashSet<>();

  public void addBoundProperty(String name, Class<?> modelClass, Node node) {
    this.propertyCandidates.put(name, Pair.of(modelClass, node));
  }

  public void bindChangedModel(ObservableValue<?> observable, Object oldValue, Object newValue) {
    if (oldValue == newValue) { //yes, no equals needed
      fireValueChangedEvent();
    } else {
      unbind(oldValue);
      bind(newValue);
    }
  }

  @SuppressWarnings("unchecked")
  public void unbind(Object oldValue) {
    for (Pair<JavaBeanProperty<?>, Property<?>> pair : bindings) {
      Property right = pair.getRight();
      JavaBeanProperty left = pair.getLeft();
      right.unbindBidirectional(left);
    }
  }

  public void fireValueChangedEvent() {
    for (JavaBeanProperty<?> property : properties.values()) {
      property.fireValueChangedEvent();
    }
  }

  public void bind(Object object) {
    for (Map.Entry<String, Pair<Class<?>, Node>> entry : propertyCandidates.entrySet()) {
      Pair<Class<?>, Node> value = entry.getValue();
      String name = entry.getKey();
      List<Field> allFields = ReflectionUtil.getAllFields(value.getLeft(), (f) -> f.getName().equals(name));
      if (allFields.size() == 1) {
        Field field = allFields.get(0);
        Class<?> fieldType = field.getType();
        JavaBeanProperty<?> property = getProperty(name, fieldType, object);
        this.properties.put(name, property);
        Node right = value.getRight();
        bindNode(right, property, fieldType);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void bindNode(Node node, JavaBeanProperty<?> property, Class<?> fieldType) {
    if (node instanceof TextInputControl) {
      TextInputControl textInputControl = (TextInputControl) node;
      StringConverter converter = getStringConverter(fieldType);
      if (converter != null) {
        LastValueConverter wrappedConverter = new LastValueConverter(converter, property);
        textInputControl.textProperty().bindBidirectional(property, wrappedConverter);
      } else {
        textInputControl.textProperty().bindBidirectional((Property<String>) property);
      }
      bindings.add(Pair.of(property, textInputControl.textProperty()));
    }
  }

  private StringConverter getStringConverter(Class<?> type) {
    type = Primitives.wrap(type);
    if (Integer.class.equals(type)) {
      return new IntegerStringConverter();
    } else if (Long.class.equals(type)) {
      return new LongStringConverter();
    } else if (Double.class.equals(type)) {
      return new DoubleStringConverter();
    } else if (Float.class.equals(type)) {
      return new FloatStringConverter();
    } else if (Boolean.class.equals(type)) {
      return new BooleanStringConverter();
    } else {
      return null;
    }
  }

  private JavaBeanProperty<?> getProperty(String name, Class<?> type, Object object) {
    try {
      type = Primitives.wrap(type);
      if (Integer.class.equals(type)) {
        return JavaBeanIntegerPropertyBuilder.create().bean(object).name(name).build();
      } else if (Long.class.equals(type)) {
        return JavaBeanLongPropertyBuilder.create().bean(object).name(name).build();
      } else if (Double.class.equals(type)) {
        return JavaBeanDoublePropertyBuilder.create().bean(object).name(name).build();
      } else if (Float.class.equals(type)) {
        return JavaBeanFloatPropertyBuilder.create().bean(object).name(name).build();
      } else if (Boolean.class.equals(type)) {
        return JavaBeanBooleanPropertyBuilder.create().bean(object).name(name).build();
      } else if (String.class.equals(type)) {
        return JavaBeanStringPropertyBuilder.create().bean(object).name(name).build();
      } else {
        return JavaBeanObjectPropertyBuilder.create().bean(object).name(name).build();
      }
    } catch (NoSuchMethodException e) {
      log.error("Could not resolve {} of {}", name, object.getClass());
      return null;
    }
  }
}
