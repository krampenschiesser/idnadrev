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
import de.ks.activity.ActivityHint;
import de.ks.activity.context.ActivityStore;
import de.ks.file.FileViewController;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.thought.view.ViewThoughtsActivity;
import de.ks.validation.ValidationRegistry;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Supplier;

public class CreateTask implements Initializable {
  @FXML
  MainTaskInfo mainInfoController;
  @FXML
  TaskOutcome expectedOutcomeController;
  @FXML
  EffortInfo effortInfoController;
  @FXML
  FileViewController fileViewController;
  @FXML
  TaskSchedule scheduleController;
  @FXML
  Button saveBtn;

  @Inject
  protected ActivityController controller;
  @Inject
  protected ActivityStore store;
  @Inject
  protected ValidationRegistry validationRegistry;

  @FXML
  public void save() {
    CreateTaskDS datasource = (CreateTaskDS) store.getDatasource();
    if (datasource.isFromThought() && mainInfoController.isProject()) {
      controller.save();
      ActivityHint hint = new ActivityHint(CreateTaskActivity.class);
      hint.setReturnToActivity(ViewThoughtsActivity.class.getSimpleName());

      Task parent = store.getModel();
      Supplier supplier = () -> {
        Task child = new Task("");
        child.setParent(parent);
        child.setContext(parent.getContext());
        return child;
      };
      hint.setDataSourceHint(supplier);
      controller.stopCurrentStartNew(hint);
    } else {
      controller.save();
      controller.stopCurrent();
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    saveBtn.disableProperty().bind(validationRegistry.invalidProperty());
  }
}
