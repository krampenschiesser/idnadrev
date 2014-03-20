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

import de.ks.JFXCDIRunner;
import de.ks.javafx.converter.LastValueConverter;
import javafx.beans.property.adapter.JavaBeanIntegerProperty;
import javafx.beans.property.adapter.JavaBeanIntegerPropertyBuilder;
import javafx.scene.control.TextField;
import javafx.util.converter.NumberStringConverter;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 *
 */
@RunWith(JFXCDIRunner.class)
public class PojoBindingTest {

  @Test
  public void testUIPojoCommunication() throws Exception {
    MyBindingPojo hello = new MyBindingPojo(1, "hello");

    TextField nameInput = new TextField();
    TextField versionInput = new TextField();

    JavaBeanIntegerProperty javaBeanIntegerProperty = JavaBeanIntegerPropertyBuilder.create().bean(hello).name("version").build();
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
}
