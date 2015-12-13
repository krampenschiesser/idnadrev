/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.idnadrev.cost.pattern.create;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class CreateEditPatternController extends BaseController<BookingPattern> {
  @FXML
  protected CheckBox contains;
  @FXML
  protected TextField name;
  @FXML
  protected TextField pattern;
  @FXML
  protected TextField category;
  @FXML
  protected Button save;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    Binding binding = store.getBinding();

    StringProperty nameBinding = binding.getStringProperty(BookingPattern.class, b -> b.getName());
    name.textProperty().bindBidirectional(nameBinding);
    StringProperty regexPattern = binding.getStringProperty(BookingPattern.class, b -> b.getRegex());
    pattern.textProperty().bindBidirectional(regexPattern);
    StringProperty categoryPattern = binding.getStringProperty(BookingPattern.class, b -> b.getCategory());
    category.textProperty().bindBidirectional(categoryPattern);
    BooleanProperty containsBinding = binding.getBooleanProperty(BookingPattern.class, b -> b.isSimpleContains());
    contains.selectedProperty().bindBidirectional(containsBinding);

    Arrays.asList(name, pattern, category).forEach(t -> validationRegistry.registerValidator(t, new NotEmptyValidator()));
    validationRegistry.registerValidator(pattern, new RegexPatternValidator());
    save.disableProperty().bind(validationRegistry.invalidProperty());
  }

  @FXML
  protected void onSave() {
    controller.save();
    controller.stopCurrent();
  }
}
