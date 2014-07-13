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
import de.ks.activity.initialization.DataStoreCallback;
import de.ks.application.fxml.DefaultLoader;
import de.ks.eventsystem.bus.HandlingThread;
import de.ks.eventsystem.bus.Threading;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Tag;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.selection.NamedPersistentObjectSelection;
import de.ks.idnadrev.tag.TagInfo;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.entity.NamedPersistentObject;
import de.ks.reflection.PropertyPath;
import de.ks.text.AsciiDocEditor;
import de.ks.validation.FXValidators;
import de.ks.validation.ValidationRegistry;
import de.ks.validation.validators.DurationValidator;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import javax.inject.Inject;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ModelBound(Task.class)
public class MainTaskInfo implements Initializable, DataStoreCallback<Task> {
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
  protected AsciiDocEditor description;
  @FXML
  protected TextField estimatedTimeDuration;
  @FXML
  protected FlowPane tagPane;
  @FXML
  protected Button saveButton;
  @FXML
  protected Slider funFactor;
  @FXML
  protected Slider mentalEffort;
  @FXML
  protected Slider physicalEffort;

  @Inject
  ValidationRegistry validationRegistry;
  @Inject
  ActivityController controller;
  @Inject
  ActivityStore store;
  private DurationValidator durationValidator;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    AsciiDocEditor.load(descriptionContainer.getChildren()::add, ade -> this.description = ade);

    description.hideActionBar();
    StringProperty descriptionBinding = store.getBinding().getStringProperty(Task.class, t -> t.getDescription());
    descriptionBinding.bind(description.textProperty());

    parentProjectController.disableProperty().bind(project.selectedProperty());
    project.disableProperty().bind(parentProjectController.getInput().textProperty().isNotEmpty());

    String projectKey = PropertyPath.property(Task.class, (t) -> t.isProject());
    parentProjectController.from(Task.class, (root, query, builder) -> {
      query.where(builder.isTrue(root.get(projectKey)));
    }).enableValidation();

    contextController.from(Context.class).enableValidation();
    tagAddController.from(Tag.class);

    durationValidator = FXValidators.createDurationValidator();

    Platform.runLater(() -> {
      Callback<AutoCompletionBinding.ISuggestionRequest, Collection<String>> estimatedTimeAutoCompletion = getEstimatedTimeAutoCompletion();
      TextFields.bindAutoCompletion(estimatedTimeDuration, estimatedTimeAutoCompletion);
    });
    validationRegistry.getValidationSupport().registerValidator(estimatedTimeDuration, durationValidator);

    project.selectedProperty().bind(store.getBinding().getBooleanProperty(Task.class, (t) -> t.isProject()).not());

    tagAddController.setOnAction(e -> addTag(tagAddController.getInput().getText()));
    saveButton.disableProperty().bind(validationRegistry.getValidationSupport().invalidProperty());

    physicalEffort.valueProperty().bindBidirectional(store.getBinding().getIntegerProperty(Task.class, (t) -> t.getPhysicalEffort().getAmount()));
    mentalEffort.valueProperty().bindBidirectional(store.getBinding().getIntegerProperty(Task.class, (t) -> t.getMentalEffort().getAmount()));
    funFactor.valueProperty().bindBidirectional(store.getBinding().getIntegerProperty(Task.class, (t) -> t.getFunFactor().getAmount()));
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
    CreateTaskDS datasource = (CreateTaskDS) store.getDatasource();
    controller.resumePreviousActivity();
  }

  public Duration getEstimatedDuration() {
    return durationValidator.getDuration();
  }

  @Override
  public void duringLoad(Task model) {

  }

  @Override
  public void duringSave(Task task) {

    String contextName = contextController.getInput().textProperty().getValueSafe().trim();
    setToOne(task, Context.class, contextName, task::setContext);

    task.setEstimatedTime(getEstimatedDuration());

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

}
