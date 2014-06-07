/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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
package de.ks.beagle.thought.task;

import de.ks.activity.ModelBound;
import de.ks.beagle.entity.Context;
import de.ks.beagle.entity.Tag;
import de.ks.beagle.entity.Task;
import de.ks.beagle.entity.WorkType;
import de.ks.beagle.selection.NamedPersistentObjectSelection;
import de.ks.reflection.PropertyPath;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

@ModelBound(Task.class)
public class MainTaskInfo implements Initializable {
  @FXML
  protected NamedPersistentObjectSelection<Task> parentProjectController;
  @FXML
  protected NamedPersistentObjectSelection<Context> contextController;
  @FXML
  protected NamedPersistentObjectSelection<WorkType> workTypeController;
  @FXML
  protected NamedPersistentObjectSelection<Tag> tagAddController;

  @FXML
  protected TextField name;
  @FXML
  protected CheckBox project;
  @FXML
  protected TextArea description;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    String projectKey = PropertyPath.property(Task.class, (t) -> t.isProject());
    parentProjectController.disableProperty().bind(project.selectedProperty());
    project.disableProperty().bind(parentProjectController.getInput().textProperty().isNotEmpty());

    parentProjectController.from(Task.class, (root, query, builder) -> {
      query.where(builder.isTrue(root.get(projectKey)));
    });
    contextController.from(Context.class);
    workTypeController.from(WorkType.class);
    tagAddController.from(Tag.class);
  }
}
