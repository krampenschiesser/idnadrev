/*
 * Copyright [${YEAR}] [Christian Loehnert]
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

package javafx.beans;

import de.ks.LauncherRunner;
import de.ks.javafx.converter.LastValueConverter;
import javafx.beans.binding.Bindings;
import javafx.beans.property.adapter.JavaBeanIntegerProperty;
import javafx.beans.property.adapter.JavaBeanIntegerPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import javafx.util.converter.NumberStringConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.*;

/**
 *
 */
@RunWith(LauncherRunner.class)
public class PojoBindingTest {
  private static final Logger log = LoggerFactory.getLogger(PojoBindingTest.class);

  @Test
  public void testUIPojoCommunication() throws Exception {
    MyBindingPojo hello = new MyBindingPojo(1, "hello");

    TextField versionInput = new TextField();

    JavaBeanIntegerPropertyBuilder builder = JavaBeanIntegerPropertyBuilder.create().beanClass(MyBindingPojo.class);
    builder.bean(hello);
    JavaBeanIntegerProperty javaBeanIntegerProperty = builder.name("version").build();
    assertEquals(1, javaBeanIntegerProperty.get());
    hello.setVersion(2);
    assertEquals(2, javaBeanIntegerProperty.get());


    versionInput.textProperty().bindBidirectional(javaBeanIntegerProperty, new NumberStringConverter());
    assertEquals("2", versionInput.getText());

    hello.setVersion(3);
    javaBeanIntegerProperty.fireValueChangedEvent();
    assertEquals("3", versionInput.getText());

    hello.setVersion(4);
    javaBeanIntegerProperty.get();
    assertEquals("3", versionInput.getText());

    //Back communication from UI
    versionInput.setText("1");
    assertEquals(1, javaBeanIntegerProperty.get());
    assertEquals(1, hello.getVersion());

    versionInput.setText("abc");
    assertEquals(0, javaBeanIntegerProperty.get());
    assertEquals(0, hello.getVersion());

    versionInput.textProperty().unbindBidirectional(javaBeanIntegerProperty);
    versionInput.setText("5");
    assertEquals(0, javaBeanIntegerProperty.get());
    assertEquals(0, hello.getVersion());

    versionInput.textProperty().bindBidirectional(javaBeanIntegerProperty, new LastValueConverter<>(new NumberStringConverter(), javaBeanIntegerProperty));
    assertEquals(0, javaBeanIntegerProperty.get());
    assertEquals(0, hello.getVersion());
    assertEquals("0", versionInput.getText());

    versionInput.setText("1");
    assertEquals(1, javaBeanIntegerProperty.get());
    assertEquals(1, hello.getVersion());

    versionInput.setText("abc");
    assertEquals(1, javaBeanIntegerProperty.get());
    assertEquals(1, hello.getVersion());


    javaBeanIntegerProperty.set(8);
    assertEquals(8, hello.getVersion());
    assertEquals("8", versionInput.getText());
  }

  @Test
  public void testListTableBinding() throws Exception {
    MyBindingPojo hello = new MyBindingPojo(1, "hello");
    for (int i = 0; i < 10; i++) {
      hello.getCollection().add(String.valueOf(i));
    }
    ObservableList<String> observableList = FXCollections.observableArrayList();
    observableList.add("Bla");

    @SuppressWarnings("unchecked") JavaBeanObjectProperty<List<String>> collection = JavaBeanObjectPropertyBuilder.create().bean(hello).name("collection").build();
    collection.addListener((observable, oldValue, newValue) -> {
      log.info("Oldvalue ={}", oldValue);
      log.info("Oldvalue ={}", newValue);
    });

    observableList.addListener((ListChangeListener<String>) c -> {
      while (c.next()) {
        log.info("Added to 'observableList' {}", c.getAddedSubList());
        log.info("Removed  from 'observableList' {}", c.getRemoved());
      }
    });
    ObservableList<String> wrappedObservable = FXCollections.observableList(collection.get());
    wrappedObservable.addListener((ListChangeListener<String>) c -> {
      while (c.next()) {
        log.info("Added to 'wrappedObservable' {}", c.getAddedSubList());
        log.info("Removed  from 'wrappedObservable' {}", c.getRemoved());
      }
    });

    Bindings.bindContentBidirectional(observableList, wrappedObservable);

    log.info("Adding to wrapped collection");
    wrappedObservable.add("Hello");
    assertTrue(hello.getCollection().contains("Hello"));
    log.info("Removing from wrapped collection");
    wrappedObservable.remove("Hello");
    assertFalse(hello.getCollection().contains("Hello"));


    log.info("Adding to observableList");
    observableList.add("Hello");
    assertTrue(hello.getCollection().contains("Hello"));
    log.info("Removing from observableList");
    observableList.remove("Hello");
    assertFalse(hello.getCollection().contains("Hello"));
  }
}
