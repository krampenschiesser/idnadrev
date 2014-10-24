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

import de.ks.BaseController;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.TaskState;
import de.ks.idnadrev.selection.NamedPersistentObjectSelection;
import de.ks.idnadrev.tag.TagContainer;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.entity.NamedPersistentObject;
import de.ks.reflection.PropertyPath;
import de.ks.text.AsciiDocEditor;
import de.ks.validation.validators.DurationValidator;
import de.ks.validation.validators.NamedEntityMustNotExistValidator;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class MainTaskInfo extends BaseController<Task> {
  @FXML
  protected NamedPersistentObjectSelection<Task> parentProjectController;
  @FXML
  protected NamedPersistentObjectSelection<Context> contextController;
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

  protected AsciiDocEditor description;
  protected DurationValidator durationValidator;
  protected Runnable saveRunnable;
  protected final ObservableList<TaskState> states = FXCollections.observableArrayList();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    AsciiDocEditor.load(descriptionContainer.getChildren()::add, ade -> this.description = ade);

    validationRegistry.registerBeanValidationValidator(name, Task.class, PropertyPath.of(Task.class, t -> t.getName()).getPropertyPath());
    validationRegistry.registerValidator(name, new NamedEntityMustNotExistValidator<>(Task.class, t -> t.getId() == store.<Task>getModel().getId()));
    description.hideActionBar();
    StringProperty nameBinding = store.getBinding().getStringProperty(Task.class, t -> t.getName());
    name.textProperty().bindBidirectional(nameBinding);
    StringProperty descriptionBinding = store.getBinding().getStringProperty(Task.class, t -> t.getDescription());
    descriptionBinding.bindBidirectional(description.textProperty());

    String projectKey = PropertyPath.property(Task.class, (t) -> t.isProject());
    parentProjectController.from(Task.class, (root, query, builder) -> {
      query.where(builder.isTrue(root.get(projectKey)));
    }).enableValidation();

    contextController.from(Context.class).enableValidation();

    durationValidator = new DurationValidator();

    Platform.runLater(() -> {
      Callback<AutoCompletionBinding.ISuggestionRequest, Collection<String>> estimatedTimeAutoCompletion = getEstimatedTimeAutoCompletion();
      TextFields.bindAutoCompletion(estimatedTimeDuration, estimatedTimeAutoCompletion);
    });
    validationRegistry.registerValidator(estimatedTimeDuration, durationValidator);

    project.setText("");
    project.selectedProperty().bindBidirectional(store.getBinding().getBooleanProperty(Task.class, (t) -> t.isProject()));


    state.setItems(states);
    states.add(TaskState.NONE);
    states.add(TaskState.ASAP);
    states.add(TaskState.DELEGATED);
    states.add(TaskState.LATER);

  }

  private Callback<AutoCompletionBinding.ISuggestionRequest, Collection<String>> getEstimatedTimeAutoCompletion() {
    return param -> {
      try {
        String userText = param.getUserText().trim();
        Integer.parseInt(userText);
        return Arrays.asList(userText + "min", userText + "hours");
      } catch (NumberFormatException e) {
        return Collections.emptyList();
      }
    };
  }

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
      contextController.getInput().setText(model.getContext().getName());
    } else {
      contextController.getInput().setText("");
    }
    if (model.getParent() != null) {
      parentProjectController.getInput().setText(model.getParent().getName());
    } else {
      parentProjectController.getInput().setText("");
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
    String contextName = contextController.getInput().textProperty().getValueSafe().trim();
    setToOne(task, Context.class, contextName, task::setContext);

    String parentProject = parentProjectController.getInput().textProperty().getValueSafe().trim();
    setToOne(task, Task.class, parentProject, task::setParent);

    task.setEstimatedTime(getEstimatedDuration());
    task.setState(state.getValue());
  }

  private <T extends NamedPersistentObject<T>> void setToOne(Task model, Class<T> clazz, String name, Consumer<T> consumer) {
    if (!name.isEmpty()) {
      T found = PersistentWork.forName(clazz, name);
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
}
