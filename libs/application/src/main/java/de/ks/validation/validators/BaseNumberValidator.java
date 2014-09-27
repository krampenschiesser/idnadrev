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

import de.ks.i18n.Localized;
import javafx.scene.control.Control;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;

import java.util.function.Function;

class BaseNumberValidator<T extends Number> implements Validator<String> {
  protected final Function<String, T> parser;
  protected final String msg;

  public BaseNumberValidator(Function<String, T> parser, String msg) {
    this.parser = parser;
    this.msg = msg;
  }

  @Override
  public ValidationResult apply(Control control, String s) {
    if (s == null || s.isEmpty()) {
      return null;
    }
    try {
      T value = parser.apply(s);
      return furtherProcessing(control, value);
    } catch (NumberFormatException e) {
      String validationMsg = Localized.get(msg);
      return ValidationResult.fromError(control, validationMsg);
    }
  }

  protected ValidationResult furtherProcessing(Control control, T value) {
    return null;
  }
}