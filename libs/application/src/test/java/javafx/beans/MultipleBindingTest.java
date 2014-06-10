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
package javafx.beans;

import de.ks.LauncherRunner;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TextField;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class MultipleBindingTest {
  @Test
  public void testName() throws Exception {
    TextField textField = new TextField();

    SimpleStringProperty first = new SimpleStringProperty();
    SimpleStringProperty second = new SimpleStringProperty();

    textField.textProperty().bindBidirectional(first);
    textField.textProperty().bindBidirectional(second);

    textField.setText("test");
    assertEquals("test", first.get());
    assertEquals("test", second.get());

    first.set("42");
    assertEquals("42", textField.textProperty().get());
    assertEquals("42", second.get());
  }
}
