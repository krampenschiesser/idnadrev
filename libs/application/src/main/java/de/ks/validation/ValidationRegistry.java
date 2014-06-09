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
package de.ks.validation;

import de.ks.activity.context.ActivityScoped;
import de.ks.activity.context.ActivityStore;
import de.ks.validation.validators.BeanValidationValidator;
import javafx.beans.property.adapter.JavaBeanProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.decoration.ValidationDecoration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.Validator;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

@ActivityScoped
public class ValidationRegistry {
  private static final Logger log = LoggerFactory.getLogger(ValidationRegistry.class);
  protected final ValidationSupport validationSupport = new ValidationSupport();
  protected final Map<String, Control> registeredControls = new HashMap<>();
  @Inject
  Validator validator;
  @Inject
  ActivityStore store;

  public ValidationRegistry() {
    ValidationDecoration iconDecorator = new CustomValidationDecoration();
//    ValidationDecoration cssDecorator = new StyleClassValidationDecoration();
//    ValidationDecoration compoundDecorator = new CompoundValidationDecoration(cssDecorator, iconDecorator);
//    validationSupport.setValidationDecorator(compoundDecorator);
    validationSupport.setValidationDecorator(iconDecorator);
  }

  public void addProperty(JavaBeanProperty<?> property, Node node) {
    Object model = store.getModel();
    if (node instanceof Control) {
      Control control = (Control) node;
      Class<?> modelClass = model.getClass();
      String propertyName = property.getName();

      BeanDescriptor constraints = validator.getConstraintsForClass(modelClass);
      PropertyDescriptor constraintsForProperty = constraints == null ? null : constraints.getConstraintsForProperty(propertyName);

      if (constraintsForProperty != null && constraintsForProperty.hasConstraints()) {
        registeredControls.put(propertyName, control);
        validationSupport.registerValidator(control, true, new BeanValidationValidator(modelClass, validator, propertyName));
        log.debug("Registered BeanValidation validator for property {} on control {}", propertyName, control);
      }
    }
  }

  public ValidationSupport getValidationSupport() {
    return validationSupport;
  }
}
