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

import de.ks.LauncherRunner;
import javafx.beans.property.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Timestamp;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class CustomBindingTest {
  private Binding binding;

  @Before
  public void setUp() throws Exception {
    binding = new Binding();
  }

  @Test
  public void testTypes() throws Exception {
    StringProperty stringProperty = binding.getStringProperty(TestObject.class, (t) -> t.getStringValue());
    BooleanProperty booleanProperty = binding.getBooleanProperty(TestObject.class, (t) -> t.getBoolValue());
    IntegerProperty integerProperty = binding.getIntegerProperty(TestObject.class, (t) -> t.getIntegerValue());
    FloatProperty floatProperty = binding.getFloatProperty(TestObject.class, (t) -> t.getFloatValue());
    ObjectProperty<Timestamp> objectProperty = binding.getObjectProperty(TestObject.class, (t) -> t.getTimestamp());
    SetProperty<String> setProperty = binding.getSetProperty(TestObject.class, (t) -> t.getSetValue());
    MapProperty<String, String> mapProperty = binding.getMapProperty(TestObject.class, (t) -> t.getMapValue());

    TestObject testObject = new TestObject();
    testObject.setBoolValue(false);
    testObject.setFloatValue(13.67F);
    testObject.setIntegerValue(12);
    testObject.setStringValue("HelloWorld");
    testObject.getMapValue().put("test", "value");
    testObject.getSetValue().add("bla");
    testObject.setTimestamp(new Timestamp(700000));

    binding.applyModelToCustomProperties(testObject);

    assertFalse(booleanProperty.get());
    assertEquals(13.67F, floatProperty.get(), 0.01F);
    assertEquals(12, integerProperty.get());
    assertEquals("HelloWorld", stringProperty.get());
    assertEquals("value", mapProperty.get().get("test"));
    assertEquals("bla", setProperty.get().iterator().next());
    assertEquals(new Timestamp(700000), objectProperty.get());

    floatProperty.setValue(42F);
    booleanProperty.set(true);
    integerProperty.set(42);
    stringProperty.set("Hello sauerland!");
    mapProperty.get().put("test2", "value2");
    setProperty.clear();
    long now = System.currentTimeMillis();
    objectProperty.set(new Timestamp(now));

    binding.applyControllerContent(testObject);

    assertTrue(testObject.getBoolValue());
    assertEquals(42F, testObject.getFloatValue(), 0.01F);
    assertEquals(42, testObject.getIntegerValue());
    assertEquals("Hello sauerland!", testObject.getStringValue());
    assertEquals("value", testObject.getMapValue().get("test"));
    assertEquals("value2", testObject.getMapValue().get("test2"));
    assertTrue(testObject.getSetValue().isEmpty());
    assertEquals(new Timestamp(now), testObject.getTimestamp());
  }
}
