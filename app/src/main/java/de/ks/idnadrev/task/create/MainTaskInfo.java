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

import com.google.inject.Injector;
import de.ks.flatadocdb.entity.NamedEntity;
import de.ks.flatjsondb.PersistentWork;
import de.ks.flatjsondb.selection.NamedEntitySelection;
import de.ks.flatjsondb.validator.NamedEntityMustNotExistValidator;
import de.ks.flatjsondb.validator.NamedEntityValidator;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.TaskState;
import de.ks.idnadrev.tag.TagContainer;
import de.ks.standbein.BaseController;
import de.ks.standbein.validation.validators.DurationValidator;
import de.ks.text.AsciiDocEditor;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import javax.inject.Inject;
import java.net.URL;
import java.time.Duration;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class MainTaskInfo extends BaseController<Task> {
  @FXML
  protected TagContainer tagAddController;

  @FXML
  protected TextField name;
  @FXML
  protected CheckBox project;
  @FXML
  protected StackPane descriptionContainer;
  @FXML
  protected TextField estimatedTimeDuration;
  @FXML
  protected ComboBox<TaskState> state;

  @Inject
  protected PersistentWork persistentWork;
  @Inject
  Injector injector;
  @Inject
  NamedEntitySelection<Task> parentProjectController;
  @Inject
  NamedEntitySelection<Context> contextController;

  protected AsciiDocEditor description;
  protected DurationValidator durationValidator;
  protected final ObservableList<TaskState> states = FXCollections.observableArrayList();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    AsciiDocEditor.load(activityInitialization, descriptionContainer.getChildren()::add, ade -> this.description = ade);

    contextController.configure(Context.class);
    parentProjectController.configure(Task.class);

    validationRegistry.registerValidator(name, new NamedEntityMustNotExistValidator<>(Task.class, t -> Objects.equals(t.getId(), store.<Task>getModel().getId()), persistentWork, localized));
    description.hideActionBar();
    StringProperty nameBinding = store.getBinding().getStringProperty(Task.class, t -> t.getName());
    name.textProperty().bindBidirectional(nameBinding);
    StringProperty descriptionBinding = store.getBinding().getStringProperty(Task.class, t -> t.getDescription());
    descriptionBinding.bindBidirectional(description.textProperty());

    validationRegistry.registerValidator(contextController.getTextField(), new NamedEntityValidator(Context.class, persistentWork, localized));
    validationRegistry.registerValidator(parentProjectController.getTextField(), new NamedEntityValidator(Task.class, persistentWork, localized));

    durationValidator = new DurationValidator(localized);
// FIXME: 12/17/15 
//    Platform.runLater(() -> {
//      Callback<AutoCompletionBinding.ISuggestionRequest, Collection<String>> estimatedTimeAutoCompletion = getEstimatedTimeAutoCompletion();
//      TextFields.bindAutoCompletion(estimatedTimeDuration, estimatedTimeAutoCompletion);
//    });
    validationRegistry.registerValidator(estimatedTimeDuration, durationValidator);

    project.setText("");
    project.selectedProperty().bindBidirectional(store.getBinding().getBooleanProperty(Task.class, Task::isProject));


    state.setItems(states);
    states.add(TaskState.NONE);
    states.add(TaskState.ASAP);
    states.add(TaskState.DELEGATED);
    states.add(TaskState.LATER);

  }

  // FIXME: 12/17/15 
//  private Callback<AutoCompletionBinding.ISuggestionRequest, Collection<String>> getEstimatedTimeAutoCompletion() {
//    return param -> {
//      try {
//        String userText = param.getUserText().trim();
//        Integer.parseInt(userText);
//        return Arrays.asList(userText + "min", userText + "hours");
//      } catch (NumberFormatException e) {
//        return Collections.emptyList();
//      }
//    };
//  }

  @Override
  protected void onRefresh(Task model) {
    this.name.requestFocus();
    this.state.setValue(model.getState());

    Duration estimatedTime = model.getEstimatedTime();

    if (estimatedTime.toMinutes() < 60) {
      estimatedTimeDuration.setText(estimatedTime.toMinutes() + "m");
    } else {
      long hours = estimatedTime.toHours();
      long minutes = estimatedTime.toMinutes() % 60;
      estimatedTimeDuration.setText(hours + ":" + String.format("%02d", minutes));
    }
    if (model.getContext() != null) {
      contextController.getTextField().setText(model.getContext().getName());
    } else {
      contextController.getTextField().setText("");
    }
    if (model.getParent() != null) {
      parentProjectController.getTextField().setText(model.getParent().getName());
    } else {
      parentProjectController.getTextField().setText("");
    }
  }

  public Duration getEstimatedDuration() {
    return durationValidator.getDuration();
  }

  @Override
  public void duringLoad(Task model) {
    if (model.getContext() != null) {
      model.getContext().getName();
    }
    if (model.getParent() != null) {
      model.getParent().getName();
    }
  }

  @Override
  public void duringSave(Task task) {
    String contextName = contextController.getTextField().textProperty().getValueSafe().trim();
    setToOne(task, Context.class, contextName, task::setContext);

    String parentProject = parentProjectController.getTextField().textProperty().getValueSafe().trim();
    setToOne(task, Task.class, parentProject, task::setParent);

    task.setEstimatedTime(getEstimatedDuration());
    task.setState(state.getValue());
  }

  private <T extends NamedEntity> void setToOne(Task model, Class<T> clazz, String name, Consumer<T> consumer) {
    if (!name.isEmpty()) {
      T found = persistentWork.read(session -> session.findByNaturalId(clazz, name));
      if (found != null) {
        consumer.accept(found);
      } else {
        //keep previous
      }
    } else {
      consumer.accept(null);
    }
  }

  public TextField getName() {
    return name;
  }

  public AsciiDocEditor getDescription() {
    return description;
  }

  public boolean isProject() {
    return project.isSelected();
  }

  public TaskState getState() {
    return state.getSelectionModel().getSelectedItem();
  }
}
