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

import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.TaskState;
import de.ks.idnadrev.thought.view.ViewThoughtsActivity;
import de.ks.standbein.BaseController;
import de.ks.standbein.activity.ActivityHint;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Supplier;

public class CreateTask extends BaseController<Task> {
  @FXML
  MainTaskInfo mainInfoController;
  @FXML
  TaskOutcome expectedOutcomeController;
  @FXML
  EffortInfo effortInfoController;
  // FIXME: 12/15/15 
//  @FXML
//  FileViewController fileViewController;
  @FXML
  TaskSchedule scheduleController;
  @FXML
  Button saveBtn;

  @FXML
  public void save() {
    CreateTaskDS datasource = (CreateTaskDS) store.getDatasource();
    TaskState state = mainInfoController.getState();
    if (datasource.isFromThought() && mainInfoController.isProject() && (state == TaskState.NONE || state == TaskState.ASAP)) {
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
