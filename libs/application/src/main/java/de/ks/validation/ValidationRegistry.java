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
package de.ks.validation;

import de.ks.activity.context.ActivityScoped;
import de.ks.activity.context.ActivityStore;
import de.ks.validation.validators.BeanValidationValidator;
import de.ks.validation.validators.ValidatorChain;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.adapter.JavaBeanProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.decoration.CompoundValidationDecoration;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;
import org.controlsfx.validation.decoration.ValidationDecoration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.validation.Validator;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ActivityScoped
public class ValidationRegistry {
  private static final Logger log = LoggerFactory.getLogger(ValidationRegistry.class);
  protected final ValidationSupport validationSupport = new ValidationSupport();
  protected final Map<String, Control> registeredControls = new HashMap<>();
  protected final Map<Control, ValidatorChain<?>> registeredValidators = new HashMap<>();

  protected final SimpleBooleanProperty invalid = new SimpleBooleanProperty(false);

  @Inject
  Validator validator;
  @Inject
  ActivityStore store;

  public ValidationRegistry() {
    ValidationDecoration iconDecorator = new CustomValidationDecoration();
    ValidationDecoration cssDecorator = new StyleClassValidationDecoration();
    ValidationDecoration compoundDecorator = new CompoundValidationDecoration(cssDecorator, iconDecorator);
    validationSupport.setValidationDecorator(compoundDecorator);
  }

  @PostConstruct
  public void init() {
    validationSupport.invalidProperty().addListener((p, o, n) -> {
      invalid.set(n || store.isLoading());
      log.trace("Validation is {}", invalid.get() ? "Invalid" : "valid");
    });
    store.loadingProperty().addListener((p, o, n) -> {
      Boolean isInvalid = validationSupport.isInvalid();
      if (isInvalid == null) {
        invalid.set(n);
      } else {
        invalid.set(n || isInvalid);
      }
      log.trace("Validation is {}", invalid.get() ? "Invalid" : "valid");
    });
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
        registerBeanValidationValidator(control, modelClass, propertyName);
        log.debug("Registered BeanValidation validator for property {} on control {}", propertyName, control);
      }
    }
  }

  public ValidationResult getValidationResult() {
    return validationSupport.getValidationResult();
  }

  public boolean isInvalid() {
    return invalid.get();
  }

  public boolean isValid() {
    return !invalid.get();
  }

  public ReadOnlyBooleanProperty invalidProperty() {
    return invalid;
  }

  public void registerBeanValidationValidator(Control control, Class<?> clazz, String propertyName) {
    registerValidator(control, true, new BeanValidationValidator(clazz, validator, propertyName));
  }

  public <T> boolean registerValidator(Control control, boolean required, org.controlsfx.validation.Validator<T> validator) {
    @SuppressWarnings("unchecked") ValidatorChain<T> validatorChain = (ValidatorChain<T>) this.registeredValidators.computeIfAbsent(control, c -> {
      ValidatorChain<Object> retval = new ValidatorChain<>();
      validationSupport.registerValidator(control, required, retval);
      return retval;
    });
    validatorChain.addValidator(validator);
    return true;
  }

  public <T> boolean registerValidator(Control control, org.controlsfx.validation.Validator<T> validator) {
    return registerValidator(control, true, validator);
  }

  public Optional<org.controlsfx.validation.ValidationMessage> getHighestMessage(Control target) {
    return validationSupport.getHighestMessage(target);
  }

  public Set<Control> getRegisteredControls() {
    return validationSupport.getRegisteredControls();
  }

  protected ValidationSupport getValidationSupport() {
    return validationSupport;
  }
}
