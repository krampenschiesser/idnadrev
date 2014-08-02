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
import de.ks.file.FileViewController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

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

  @Inject
  protected ActivityController controller;

  public void save() {
    controller.save();
    controller.resumePreviousActivity();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    expectedOutcomeController.setSaveRunnable(this::save);
    mainInfoController.setSaveRunnable(this::save);
    effortInfoController.setSaveRunnable(this::save);
    scheduleController.setSaveRunnable(this::save);
  }
}
