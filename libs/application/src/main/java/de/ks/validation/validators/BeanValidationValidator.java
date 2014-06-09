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

import de.ks.validation.ValidationMessage;
import javafx.scene.control.Control;
import org.controlsfx.validation.ValidationResult;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BeanValidationValidator implements org.controlsfx.validation.Validator<Object> {
  private final Class<? extends Object> modelClass;
  private final Validator validator;
  private final String propertyName;

  public BeanValidationValidator(Class<? extends Object> modelClass, Validator validator, String propertyName) {
    this.modelClass = modelClass;
    this.validator = validator;
    this.propertyName = propertyName;
  }

  @Override
  public ValidationResult apply(Control control, Object o) {
    Set<? extends ConstraintViolation<?>> constraints = validator.validateValue(modelClass, propertyName, o);
    List<ValidationMessage> validationMessages = constraints.stream().map((v) -> new ValidationMessage(v.getMessageTemplate(), control, v.getInvalidValue())).collect(Collectors.toList());
    return ValidationResult.fromMessages(validationMessages);
  }
}
