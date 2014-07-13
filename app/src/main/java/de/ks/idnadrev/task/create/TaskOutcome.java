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
package de.ks.idnadrev.task.create;

import de.ks.activity.ActivityController;
import de.ks.activity.initialization.ActivityInitialization;
import de.ks.activity.initialization.DataStoreCallback;
import de.ks.idnadrev.entity.Task;
import de.ks.text.AsciiDocEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class TaskOutcome implements Initializable, DataStoreCallback<Task> {
  @FXML
  protected StackPane expectedOutcomeContainer;

  @Inject
  protected ActivityInitialization activityInitialization;
  @Inject
  protected ActivityController controller;

  protected AsciiDocEditor expectedOutcome;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    activityInitialization.loadAdditionalController(AsciiDocEditor.class).thenAcceptAsync(loader -> {
      expectedOutcome = loader.getController();
      expectedOutcomeContainer.getChildren().add(loader.getView());
    }, controller.getJavaFXExecutor());
  }

  @Override
  public void duringLoad(Task model) {
    expectedOutcome.setText(model.getOutcome().getExpectedOutcome());
  }

  @Override
  public void duringSave(Task model) {
    model.getOutcome().setExpectedOutcome(expectedOutcome.getText());
  }
}
