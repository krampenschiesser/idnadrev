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

import de.ks.flatadocdb.entity.NamedEntity;
import de.ks.flatjsondb.PersistentWork;
import de.ks.flatjsondb.selection.NamedEntitySelection;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.WorkUnit;
import de.ks.standbein.BaseController;
import de.ks.standbein.activity.executor.ActivityExecutor;
import de.ks.standbein.table.TableConfigurator;
import de.ks.texteditor.TextEditor;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FastTrack extends BaseController<Task> {
  private static final Logger log = LoggerFactory.getLogger(FastTrack.class);
  @FXML
  protected StackPane descriptionView;
  @FXML
  protected Label spentTime;
  @FXML
  protected StackPane nameContainer;
  protected TextEditor description;

  @Inject
  NamedEntitySelection<Task> selection;
  @Inject
  TableConfigurator<Task> selectTableConfigurator;
  @Inject
  PersistentWork persistentWork;

  protected SimpleObjectProperty<LocalDateTime> start = new SimpleObjectProperty<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    TextEditor.load(this.activityInitialization, descriptionView.getChildren()::add, ade -> this.description = ade);
    selection.configure(Task.class, conf -> conf.addBoolean(Task.class, Task::isProject));

    Function<String, List<Task>> tableItemSupplier = input -> {
      Collection<Task> query = persistentWork.query(Task.class, NamedEntity.nameQuery(), (String s) -> s.startsWith(input));
      ArrayList<Task> tasks = new ArrayList<>(query);
      Collections.sort(tasks, Comparator.comparing(NamedEntity::getName));
      return tasks;
    };
    Function<String, List<String>> comboValueSupplier = input -> {
      Collection<Task> query = persistentWork.query(Task.class, NamedEntity.nameQuery(), (String s) -> s.startsWith(input));
      List<String> names = query.stream().map(NamedEntity::getName).collect(Collectors.toList());
      Collections.sort(names);
      return names;
    };

    selection.setOnAction(e -> {
      String text = selection.getTextField().textProperty().getValueSafe();
      if (!text.trim().isEmpty()) {
        Task read = persistentWork.read(session -> session.findByNaturalId(Task.class, text));
        if (read != null) {
          store.setModel(read);
        } else {
          Task task = new Task(text);
          store.setModel(task);
        }
      }
    });
    StringProperty nameBinding = store.getBinding().getStringProperty(Task.class, t -> t.getName());
    nameBinding.bindBidirectional(selection.getTextField().textProperty());

    StringProperty descriptionBinding = store.getBinding().getStringProperty(Task.class, t -> t.getDescription());
    descriptionBinding.bindBidirectional(description.textProperty());

    nameContainer.getChildren().add(selection.getRoot());
  }

  private boolean isNameSet() {
    return selection.getTextField().textProperty().getValueSafe().trim().length() > 0;
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
    ActivityExecutor executorService = controller.getExecutorService();
    executorService.scheduleAtFixedRate(this::showSpentTime, 1, 1, TimeUnit.MINUTES);
    this.start.set(LocalDateTime.now());
  }

  @Override
  protected void onRefresh(Task model) {
    spentTime.setText("0min");
    start.set(LocalDateTime.now());
  }

  @Override
  public void duringSave(Task model) {
    super.duringSave(model);
    WorkUnit workUnit = new WorkUnit(model);
    workUnit.setStart(start.get());
    workUnit.setEnd(LocalDateTime.now());
    if (model.isFinished() || model.getId() == null) {
      model.setFinished(true);
    }
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
