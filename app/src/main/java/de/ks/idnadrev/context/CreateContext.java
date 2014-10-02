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
package de.ks.idnadrev.context;

import de.ks.activity.ActivityController;
import de.ks.activity.context.ActivityStore;
import de.ks.idnadrev.entity.Context;
import de.ks.reflection.PropertyPath;
import de.ks.validation.ValidationRegistry;
import de.ks.validation.validators.NamedEntityMustNotExistValidator;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class CreateContext implements Initializable {
  @FXML
  protected TextField name;
  @FXML
  protected Button saveButton;
  @Inject
  protected ActivityController controller;
  @Inject
  protected ActivityStore store;
  @Inject
  protected ValidationRegistry validationRegistry;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    StringProperty nameProperty = store.getBinding().getStringProperty(Context.class, c -> c.getName());
    name.textProperty().bindBidirectional(nameProperty);

    validationRegistry.registerValidator(name, new NamedEntityMustNotExistValidator<>(Context.class, t -> t.getId() == store.<Context>getModel().getId()));
    validationRegistry.registerBeanValidationValidator(name, Context.class, PropertyPath.property(Context.class, c -> c.getName()));

    saveButton.disableProperty().bind(validationRegistry.invalidProperty());
  }

  @FXML
  protected void save() {
    controller.save();
    controller.stopCurrent();
  }
}
