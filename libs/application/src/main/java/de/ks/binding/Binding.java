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
import de.ks.validation.ValidationRegistry;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.beans.property.adapter.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.scene.web.HTMLEditor;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class Binding {
  private static final Logger log = LoggerFactory.getLogger(Binding.class);
  private final Map<String, Pair<Class<?>, Node>> propertyCandidates = new HashMap<>();
  private final Map<String, JavaBeanProperty<?>> properties = new HashMap<>();
  private final Set<Pair<JavaBeanProperty<?>, Property<?>>> bindings = new HashSet<>();
  @Inject
  ValidationRegistry validationRegistry;

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
    Platform.runLater(() -> {
      for (JavaBeanProperty<?> property : properties.values()) {
        property.fireValueChangedEvent();
      }
    });
  }

  public void bind(Object object) {
    for (Map.Entry<String, Pair<Class<?>, Node>> entry : propertyCandidates.entrySet()) {
      Pair<Class<?>, Node> value = entry.getValue();
      String name = entry.getKey();
      Node node = value.getRight();
      Class<?> clazz = value.getLeft();
      if ("this".equals(name)) {
        bindRootObject(object, node);
      } else {
        bindAllFields(object, name, node, clazz);
      }
    }
  }

  private void bindAllFields(Object object, String name, Node node, Class<?> clazz) {
    List<Field> allFields = ReflectionUtil.getAllFields(clazz, (f) -> f.getName().equals(name));
    if (allFields.size() == 1) {
      Field field = allFields.get(0);
      Class<?> fieldType = field.getType();
      JavaBeanProperty<?> property = getProperty(name, fieldType, object);
      if (property != null) {
        validationRegistry.addProperty(property, node);
        this.properties.put(name, property);
        bindNode(node, property, fieldType);
      }
      if (Collection.class.isAssignableFrom(fieldType)) {
        bindCollectionNode(node, fieldType, name, object);
      }
    }
  }

  private void bindRootObject(Object object, Node node) {
    if (Collection.class.isAssignableFrom(object.getClass())) {
      bindCollectionNode(node, List.class, "this", object);
    }
  }

  private void bindCollectionNode(Node node, Class<?> fieldType, String name, Object object) {
    if (node instanceof TableView) {
      TableView tableView = (TableView) node;
      assignObservableCollection(tableView, fieldType, name, object);
    }
  }

  @SuppressWarnings("unchecked")
  private void assignObservableCollection(TableView tableView, Class<?> fieldType, String name, Object object) {
    if (List.class.isAssignableFrom(fieldType) && "this".equals(name)) {
      initilalizeTableColumns(tableView, fieldType, object);
      tableView.setItems(FXCollections.observableList((List<Object>) object));
    } else {
      MethodHandle listGetter = findGetter(List.class, fieldType, name, object);
      if (listGetter != null) {
        try {
          List list = (List) listGetter.invoke(object);
          initilalizeTableColumns(tableView, fieldType, list);
          tableView.setItems(FXCollections.observableList(list));
        } catch (Throwable throwable) {
          log.error("Could not get list of {} for property {}", object, name, throwable);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected void initilalizeTableColumns(TableView tableView, Class<?> listType, Object object) {
    Collection collection = (Collection) object;
    if (collection.iterator().hasNext()) {
      Object next = collection.iterator().next();
      Class<?> elementType = next.getClass();


      Map<String, Field> fieldMap = ReflectionUtil.getAllFields(elementType).stream().filter((f) -> !Modifier.isStatic(f.getModifiers())).collect(Collectors.toMap((f) -> f.getName(), (f) -> f));
      ObservableList<TableColumn> columns = (ObservableList<TableColumn>) tableView.getColumns();

      Map<TableColumn, Field> columnFieldMap = columns.stream().collect(Collectors.toMap((c) -> c, (c) -> {
        String id = c.getId();
        return fieldMap.get(id);
      }));

      for (TableColumn column : columns) {
        column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
          @Override
          public ObservableValue call(TableColumn.CellDataFeatures param) {
            String propertyName = param.getTableColumn().getId();
            Field field = columnFieldMap.get(param.getTableColumn());
            Object value = param.getValue();
            return getProperty(propertyName, field.getType(), value);
          }
        });
      }
    }
  }

  private MethodHandle findGetter(Class<?> returnType, Class<?> fieldType, String name, Object object) {
    try {
      List<Field> allFields = ReflectionUtil.getAllFields(object.getClass(), (f) -> f.getName().equals(name) && List.class.isAssignableFrom(f.getType()));
      if (allFields.size() == 1) {
        Field field = allFields.get(0);
        field.setAccessible(true);
        return MethodHandles.lookup().unreflectGetter(field);
      } else {
        log.warn("Could not find any field for {}.{}", object.getClass(), name);
      }
    } catch (IllegalAccessException e) {
      log.error("Could not access{}.{}", object.getClass(), name, e);
    }
    return null;
  }

  private void bindNode(Node node, JavaBeanProperty<?> property, Class<?> fieldType) {
    if (node instanceof TextInputControl) {
      TextInputControl textInputControl = (TextInputControl) node;
      StringProperty stringProperty = textInputControl.textProperty();
      bindStringProperty(property, fieldType, stringProperty);
    } else if (node instanceof Labeled) {
      Labeled labeled = ((Labeled) node);
      StringProperty stringProperty = labeled.textProperty();
      bindStringProperty(property, fieldType, stringProperty);
    } else if (node instanceof HTMLEditor) {
      throw new IllegalStateException("Cannot bind HTMLEditor as it has no text property");
    }
  }

  @SuppressWarnings("unchecked")
  private void bindStringProperty(JavaBeanProperty<?> property, Class<?> fieldType, StringProperty stringProperty) {
    StringConverter converter = getStringConverter(fieldType);
    if (converter != null) {
      LastValueConverter wrappedConverter = new LastValueConverter(converter, property);
      stringProperty.bindBidirectional(property, wrappedConverter);
    } else {
      stringProperty.bindBidirectional((Property<String>) property);
    }
    bindings.add(Pair.of(property, stringProperty));
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
