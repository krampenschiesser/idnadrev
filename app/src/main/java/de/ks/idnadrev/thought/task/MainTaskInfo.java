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
package de.ks.idnadrev.thought.task;

import com.google.common.eventbus.Subscribe;
import de.ks.activity.ActivityLoadFinishedEvent;
import de.ks.activity.ModelBound;
import de.ks.eventsystem.bus.HandlingThread;
import de.ks.eventsystem.bus.Threading;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Tag;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.WorkType;
import de.ks.idnadrev.selection.NamedPersistentObjectSelection;
import de.ks.reflection.PropertyPath;
import de.ks.validation.FXValidators;
import de.ks.validation.ValidationRegistry;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.inject.Inject;
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
  @FXML
  protected TextField estimatedTimeDuration;

  @Inject
  ValidationRegistry validationRegistry;

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

    validationRegistry.getValidationSupport().registerValidator(estimatedTimeDuration, FXValidators.createDurationValidator());
  }

  @Subscribe
  @Threading(HandlingThread.JavaFX)
  public void onRefresh(ActivityLoadFinishedEvent event) {
    this.name.requestFocus();
  }
}
