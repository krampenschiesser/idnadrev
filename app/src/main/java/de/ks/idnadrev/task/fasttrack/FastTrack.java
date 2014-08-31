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
package de.ks.idnadrev.task.fasttrack;

import de.ks.BaseController;
import de.ks.activity.executor.ActivityExecutor;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.WorkUnit;
import de.ks.idnadrev.selection.NamedPersistentObjectSelection;
import de.ks.persistence.PersistentWork;
import de.ks.text.AsciiDocEditor;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class FastTrack extends BaseController<Task> {
  private static final Logger log = LoggerFactory.getLogger(FastTrack.class);
  @FXML
  protected StackPane descriptionView;
  @FXML
  protected NamedPersistentObjectSelection<Task> nameController;
  @FXML
  protected Label spentTime;
  protected AsciiDocEditor description;

  protected SimpleObjectProperty<LocalDateTime> start = new SimpleObjectProperty<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    AsciiDocEditor.load(descriptionView.getChildren()::add, ade -> this.description = ade);

    nameController.from(Task.class);

    nameController.selectedValueProperty().addListener((p, o, n) -> {
      if (n != null) {
        store.setModel(n);
      } else {
        String text = nameController.getInput().getText();
        Task model = new Task(text);
        store.setModel(model);
      }
    });
    StringProperty nameBinding = store.getBinding().getStringProperty(Task.class, t -> t.getName());
    nameBinding.bindBidirectional(nameController.getInput().textProperty());

    description.hideActionBar();
    StringProperty descriptionBinding = store.getBinding().getStringProperty(Task.class, t -> t.getDescription());
    descriptionBinding.bindBidirectional(description.textProperty());
  }

  private boolean isNameSet() {
    return nameController.getInput().textProperty().getValueSafe().trim().length() > 0;
  }

  private void showSpentTime() {
    Duration duration = Duration.between(start.get(), LocalDateTime.now());
    controller.getJavaFXExecutor().execute(() -> spentTime.setText(duration.toMinutes() + "m"));
  }

  @FXML
  void finishTask() {
    if (isNameSet() && start.get() != null) {
      controller.save();
    }
    controller.stopCurrent();
  }

  @Override
  public void onStart() {
    ActivityExecutor executorService = controller.getExecutorService();
    executorService.scheduleAtFixedRate(this::showSpentTime, 1, 1, TimeUnit.MINUTES);
    onResume();
  }

  @Override
  public void onStop() {
    if (isNameSet() && start.get() != null) {
      controller.save();
    }
  }

  @Override
  public void onSuspend() {
    if (isNameSet() && start.get() != null) {
      controller.save();
    }
  }

  @Override
  public void onResume() {
    this.start.set(LocalDateTime.now());
  }

  @Override
  protected void onRefresh(Task model) {
    super.onRefresh(model);
    spentTime.setText("0min");
  }

  @Override
  public void duringSave(Task model) {
    super.duringSave(model);
    WorkUnit workUnit = new WorkUnit(model);
    workUnit.setStart(start.get());
    workUnit.setEnd(LocalDateTime.now());
    if (model.isFinished() || model.getId() == 0) {
      model.setFinished(true);
    }
    PersistentWork.persist(workUnit);
    start.set(null);
  }

  public LocalDateTime getStart() {
    return start.get();
  }

  public SimpleObjectProperty<LocalDateTime> startProperty() {
    return start;
  }

  public void setStart(LocalDateTime start) {
    this.start.set(start);
  }
}
