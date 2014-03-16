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
package de.ks.javafx.converter;

import de.ks.JFXCDIRunner;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.converter.NumberStringConverter;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 *
 */
@RunWith(JFXCDIRunner.class)
public class LastValueConverterTest {
  @Test
  public void testExcpetionSaveConversion() throws Exception {
    SimpleStringProperty stringProperty = new SimpleStringProperty();

    SimpleIntegerProperty integerProperty = new SimpleIntegerProperty();

    stringProperty.bindBidirectional(integerProperty, new LastValueConverter<>(new NumberStringConverter(), integerProperty));

    stringProperty.setValue("3");
    assertEquals(3, integerProperty.get());

    stringProperty.setValue("Unparseable");
    assertEquals(3, integerProperty.get());
    assertEquals("Unparseable", stringProperty.get());

    integerProperty.set(1);
    assertEquals("1", stringProperty.get());
  }
}
