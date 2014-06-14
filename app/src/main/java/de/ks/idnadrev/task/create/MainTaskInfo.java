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

import com.google.common.eventbus.Subscribe;
import de.ks.activity.ActivityController;
import de.ks.activity.ActivityLoadFinishedEvent;
import de.ks.activity.ModelBound;
import de.ks.activity.context.ActivityStore;
import de.ks.application.fxml.DefaultLoader;
import de.ks.eventsystem.bus.HandlingThread;
import de.ks.eventsystem.bus.Threading;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Tag;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.WorkType;
import de.ks.idnadrev.selection.NamedPersistentObjectSelection;
import de.ks.idnadrev.tag.TagInfo;
import de.ks.reflection.PropertyPath;
import de.ks.validation.FXValidators;
import de.ks.validation.ValidationRegistry;
import de.ks.validation.validators.DurationValidator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import javax.inject.Inject;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

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
  @FXML
  protected FlowPane tagPane;
  @FXML
  protected Button saveButton;

  @Inject
  ValidationRegistry validationRegistry;
  @Inject
  ActivityController controller;
  @Inject
  ActivityStore store;
  private DurationValidator durationValidator;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    parentProjectController.disableProperty().bind(project.selectedProperty());
    project.disableProperty().bind(parentProjectController.getInput().textProperty().isNotEmpty());

    String projectKey = PropertyPath.property(Task.class, (t) -> t.isProject());
    parentProjectController.from(Task.class, (root, query, builder) -> {
      query.where(builder.isTrue(root.get(projectKey)));
    }).enableValidation();

    contextController.from(Context.class).enableValidation();
    workTypeController.from(WorkType.class).enableValidation();
    tagAddController.from(Tag.class);

    durationValidator = FXValidators.createDurationValidator();

    Platform.runLater(() -> TextFields.bindAutoCompletion(estimatedTimeDuration, getEstimatedTimeAutoCompletion()));
    validationRegistry.getValidationSupport().registerValidator(estimatedTimeDuration, durationValidator);

    project.selectedProperty().bind(store.getBinding().getBooleanProperty(Task.class, (t) -> t.isProject()).not());

    tagAddController.setOnAction(e -> addTag(tagAddController.getInput().getText()));
    saveButton.disableProperty().bind(validationRegistry.getValidationSupport().invalidProperty());
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

  @Subscribe
  @Threading(HandlingThread.JavaFX)
  public void onRefresh(ActivityLoadFinishedEvent event) {
    this.name.requestFocus();
  }

  @FXML
  void save() {
    controller.save();
  }

  public Duration getEstimatedDuration() {
    return durationValidator.getDuration();
  }

}
