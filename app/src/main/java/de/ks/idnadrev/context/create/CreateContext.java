/*
 * Copyright [2015] [Christian Loehnert]
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
package de.ks.idnadrev.context.create;

import de.ks.flatadocdb.entity.NamedEntity;
import de.ks.flatjsondb.PersistentWork;
import de.ks.flatjsondb.validator.NamedEntityMustNotExistValidator;
import de.ks.idnadrev.entity.Context;
import de.ks.standbein.BaseController;
import de.ks.standbein.validation.validators.NotEmptyValidator;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class CreateContext extends BaseController<Context> {
  @FXML
  protected TextField name;
  @FXML
  protected Button saveButton;

  @Inject
  PersistentWork persistentWork;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    StringProperty nameProperty = store.getBinding().getStringProperty(Context.class, NamedEntity::getName);
    name.textProperty().bindBidirectional(nameProperty);

    validationRegistry.registerValidator(name, new NamedEntityMustNotExistValidator<Context>(Context.class, context -> context.getId() == store.<Context>getModel().getId(), persistentWork, localized));
    validationRegistry.registerValidator(name, new NotEmptyValidator(localized));

    saveButton.disableProperty().bind(validationRegistry.invalidProperty());
  }

  @FXML
  protected void save() {
    controller.save();
    controller.stopCurrent();
  }
}
