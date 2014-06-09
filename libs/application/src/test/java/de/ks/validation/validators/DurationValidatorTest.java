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
package de.ks.validation.validators;

import de.ks.LauncherRunner;
import javafx.scene.control.Control;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(LauncherRunner.class)
public class DurationValidatorTest {
  private DurationValidator validator;
  private Control control;

  @Before
  public void setUp() throws Exception {
    validator = new DurationValidator();
    control = null;
  }

  @Test
  public void testInvalid() throws Exception {
    assertNotNull(validator.apply(control, "13"));
    assertNotNull(validator.apply(control, "min"));
    assertNotNull(validator.apply(control, "m"));
    assertNotNull(validator.apply(control, "asd"));
    assertNotNull(validator.apply(control, "13:99"));
    assertNotNull(validator.apply(control, "13:1"));
    assertNotNull(validator.apply(control, "13:60"));
  }

  @Test
  public void testMinutes() throws Exception {
    assertNull(validator.apply(control, "   "));
    assertNull(validator.apply(control, "13m"));
    assertNull(validator.apply(control, "13min"));
    assertNull(validator.apply(control, "13  min  "));
    assertNull(validator.apply(control, "0:13"));
  }

  @Test
  public void testHours() throws Exception {
    assertNull(validator.apply(control, "   "));
    assertNull(validator.apply(control, "13h"));
    assertNull(validator.apply(control, "13hours"));
    assertNull(validator.apply(control, "13  hours  "));
    assertNull(validator.apply(control, "93:13"));
    assertNull(validator.apply(control, "3:03"));

  }
}

