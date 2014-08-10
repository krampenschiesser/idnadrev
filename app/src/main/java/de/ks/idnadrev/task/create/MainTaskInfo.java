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
import de.ks.activity.ModelBound;
import de.ks.application.fxml.DefaultLoader;
import de.ks.executor.group.LastTextChange;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Tag;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.TaskState;
import de.ks.idnadrev.selection.NamedPersistentObjectSelection;
import de.ks.idnadrev.tag.TagInfo;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ModelBound(Task.class)
public class MainTaskInfo extends BaseController<Task> {
  @FXML
  protected NamedPersistentObjectSelection<Task> parentProjectController;
  @FXML
  protected NamedPersistentObjectSelection<Context> contextController;
  @FXML
  protected NamedPersistentObjectSelection<Tag> tagAddController;

  @FXML
  protected TextField name;
  @FXML
  protected CheckBox project;
  @FXML
  protected StackPane descriptionContainer;
  @FXML
  protected TextField estimatedTimeDuration;
  @FXML
  protected FlowPane tagPane;
  @FXML
  protected ComboBox<TaskState> state;

  protected AsciiDocEditor description;
  protected DurationValidator durationValidator;
  protected Runnable saveRunnable;
  protected final ObservableList<TaskState> states = FXCollections.observableArrayList();
  private LastTextChange lastNameChange;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    AsciiDocEditor.load(descriptionContainer.getChildren()::add, ade -> this.description = ade);

    validationRegistry.registerValidator(name, new NamedEntityMustNotExistValidator<>(Task.class, t -> t.getId() == store.<Task>getModel().getId()));
    description.hideActionBar();
    StringProperty nameBinding = store.getBinding().getStringProperty(Task.class, t -> t.getName());
    name.textProperty().bindBidirectional(nameBinding);
    StringProperty descriptionBinding = store.getBinding().getStringProperty(Task.class, t -> t.getDescription());
    descriptionBinding.bindBidirectional(description.textProperty());

    parentProjectController.disableProperty().bind(project.selectedProperty());
    project.disableProperty().bind(parentProjectController.getInput().textProperty().isNotEmpty());

    String projectKey = PropertyPath.property(Task.class, (t) -> t.isProject());
    parentProjectController.from(Task.class, (root, query, builder) -> {
      query.where(builder.isTrue(root.get(projectKey)));
    }).enableValidation();

    contextController.from(Context.class).enableValidation();
    tagAddController.from(Tag.class);

    durationValidator = new DurationValidator();

    Platform.runLater(() -> {
      Callback<AutoCompletionBinding.ISuggestionRequest, Collection<String>> estimatedTimeAutoCompletion = getEstimatedTimeAutoCompletion();
      TextFields.bindAutoCompletion(estimatedTimeDuration, estimatedTimeAutoCompletion);
    });
    validationRegistry.registerValidator(estimatedTimeDuration, durationValidator);

    project.setText("");
    project.selectedProperty().bindBidirectional(store.getBinding().getBooleanProperty(Task.class, (t) -> t.isProject()));

    tagAddController.setOnAction(e -> addTag(tagAddController.getInput().getText()));

    state.setItems(states);
    states.add(TaskState.NONE);
    states.add(TaskState.ASAP);
    states.add(TaskState.DELEGATED);
    states.add(TaskState.LATER);

    lastNameChange = new LastTextChange(name, controller.getCurrentExecutorService());
    lastNameChange.registerHandler(cf -> {
      cf.thenAcceptAsync(name -> {
        String desc = description.textProperty().getValueSafe().trim();
        if (desc.isEmpty() || (desc.startsWith("= ") && name.contains(desc.substring(2)))) {
          description.setText("= " + name + "\n");
        }
      }, controller.getJavaFXExecutor());
    });
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

  private void addTag(String tag) {
    CompletableFuture.supplyAsync(() -> {
      DefaultLoader<GridPane, TagInfo> loader = new DefaultLoader<>(TagInfo.class);
      loader.load();
      return loader;
    }, controller.getCurrentExecutorService()).thenAcceptAsync((loader) -> {
      TagInfo ctrller = loader.getController();
      ctrller.getName().setText(tag);
      GridPane view = loader.getView();
      view.setId(tag);
      ctrller.getRemove().setOnAction((e) -> tagPane.getChildren().remove(view));
      tagPane.getChildren().add(view);
    }, controller.getJavaFXExecutor());
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
  }

  public Duration getEstimatedDuration() {
    return durationValidator.getDuration();
  }

  @Override
  public void duringSave(Task task) {

    String contextName = contextController.getInput().textProperty().getValueSafe().trim();
    setToOne(task, Context.class, contextName, task::setContext);
    String parentProject = parentProjectController.getInput().textProperty().getValueSafe().trim();
    if (!parentProject.isEmpty()) {
      setToOne(task, Task.class, parentProject, task::setParent);
    }

    task.setEstimatedTime(getEstimatedDuration());
    task.setState(state.getValue());

    tagPane.getChildren().stream().map(c -> new Tag(c.getId())).forEach(tag -> {
      Tag readTag = PersistentWork.forName(Tag.class, tag.getName());
      readTag = readTag == null ? tag : readTag;
      task.addTag(readTag);
    });
  }

  private <T extends NamedPersistentObject<T>> void setToOne(Task model, Class<T> clazz, String contextName, Consumer<T> consumer) {
    if (!contextName.isEmpty()) {
      Optional<T> first = PersistentWork.forNameLike(clazz, contextName).stream().findFirst();
      if (first.isPresent()) {
        consumer.accept(first.get());
      }
    }
  }

  public TextField getName() {
    return name;
  }

  public AsciiDocEditor getDescription() {
    return description;
  }
}
