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
package de.ks.idnadrev;

import de.ks.activity.ActivityController;
import de.ks.idnadrev.task.create.CreateTaskActivity;
import de.ks.idnadrev.task.view.ViewTasksActvity;
import de.ks.idnadrev.thought.collect.ThoughtActivity;
import de.ks.idnadrev.thought.view.ViewThoughtsActivity;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class ButtonBar implements Initializable {
  @Inject
  ActivityController controller;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    //
  }

  @FXML
  void addThought() {
    controller.start(ThoughtActivity.class);
  }

  @FXML
  void viewThoughts() {
    controller.start(ViewThoughtsActivity.class);
  }

  @FXML
  void createTask() {
    controller.start(CreateTaskActivity.class);
  }

  @FXML
  void viewTasks() {
    controller.start(ViewTasksActvity.class);
  }

}
