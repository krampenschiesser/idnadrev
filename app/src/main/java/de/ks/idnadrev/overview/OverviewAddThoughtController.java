/**
 * Copyright [2014] [Christian Loehnert]
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
package de.ks.idnadrev.overview;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class OverviewAddThoughtController extends BaseController<OverviewModel> {
  private static final Logger log = LoggerFactory.getLogger(OverviewAddThoughtController.class);

  @FXML
  protected TextField name;
  @FXML
  protected Button save;
  @FXML
  protected StackPane descriptionContainer;

  protected AsciiDocEditor description;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    AsciiDocEditor.load(descriptionContainer.getChildren()::add, this::setDescription);

    validationRegistry.registerValidator(name, new NotEmptyValidator());
    validationRegistry.registerValidator(name, new NamedEntityMustNotExistValidator(Thought.class));

    save.disableProperty().bind(validationRegistry.invalidProperty());

  }

  protected void setDescription(AsciiDocEditor description) {
    this.description = description;
    description.hideActionBar();
  }

  @FXML
  protected void onSave() {
    String nameText = name.getText();
    String descriptionText = description.getText();

    CompletableFuture.runAsync(() -> {
      Thought thought = new Thought(nameText);
      PersistentWork.persist(thought.setDescription(descriptionText));
    }, controller.getExecutorService())//
      .thenRunAsync(() -> {
        name.setText("");
        description.setText("");
      }, controller.getJavaFXExecutor());
  }
}
