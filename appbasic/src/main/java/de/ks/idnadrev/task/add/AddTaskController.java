/*
 * Copyright [2016] [Christian Loehnert]
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
package de.ks.idnadrev.task.add;

import de.ks.idnadrev.adoc.AdocFile;
import de.ks.idnadrev.adoc.ui.TagSelection;
import de.ks.idnadrev.crud.CRUDController;
import de.ks.idnadrev.index.Index;
import de.ks.idnadrev.index.MultiQueyBuilder;
import de.ks.idnadrev.index.StandardQueries;
import de.ks.idnadrev.repository.ui.ActiveRepositoryController;
import de.ks.idnadrev.task.Task;
import de.ks.idnadrev.task.TaskState;
import de.ks.idnadrev.util.AdocTransformer;
import de.ks.idnadrev.util.HasToExistValidator;
import de.ks.standbein.BaseController;
import de.ks.standbein.autocomp.AutoCompletionTextField;
import de.ks.standbein.validation.validators.NotEmptyValidator;
import de.ks.texteditor.TextEditor;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.util.converter.NumberStringConverter;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AddTaskController extends BaseController<Task> {

  @FXML
  TextField estimatedTime;
  @FXML
  CRUDController crudController;
  @FXML
  ComboBox<String> state;
  @FXML
  StackPane parentContainer;
  @FXML
  StackPane contextContainer;
  @FXML
  StackPane editorContainer;
  @FXML
  TextField title;
  @FXML
  TagSelection tagsController;
  @FXML
  ActiveRepositoryController repositoryController;
  @FXML
  AddTaskDetails detailsController;

  @Inject
  AutoCompletionTextField context;
  @Inject
  AutoCompletionTextField parent;
  private TextEditor editor;

  @Inject
  Index index;
  @Inject
  AdocTransformer transformer;

  private final List<String> projects = new ArrayList<>();
  private final List<String> contexts = new ArrayList<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    TextEditor.load(activityInitialization, editorContainer.getChildren()::add, ctrl -> editor = ctrl);

    Button saveButton = crudController.getSaveButton();
    saveButton.setVisible(true);
    saveButton.setOnAction(e -> {
      controller.save();
      AddTaskDs datasource = (AddTaskDs) store.getDatasource();
      if (datasource.hasHint()) {
        controller.stopCurrent();
      } else {
        controller.reload();
      }
    });

    context.setEditable(true);
    context.configure(this::getContexts);
    parent.configure(this::getParents);
    validationRegistry.registerValidator(parent.getTextField(), new HasToExistValidator(index, localized));

    contextContainer.getChildren().add(context.getTextField());
    parentContainer.getChildren().add(parent.getTextField());

    state.getItems().addAll(Stream.of(TaskState.values()).filter(s -> s != TaskState.UNPROCESSED).map(TaskState::name).collect(Collectors.toList()));
    state.getSelectionModel().select(0);

    tagsController.setEditable(true);
    editor.textProperty().bindBidirectional(store.getBinding().getStringProperty(Task.class, Task::getContent));
    title.textProperty().bindBidirectional(store.getBinding().getStringProperty(Task.class, t -> t.getHeader().getTitle()));
    context.getTextField().textProperty().bindBidirectional(store.getBinding().getStringProperty(Task.class, Task::getContext));

    estimatedTime.textProperty().bindBidirectional(store.getBinding().getIntegerProperty(Task.class, Task::getEstimatedTimeInMinutes), new NumberStringConverter());
    validationRegistry.registerValidator(title, new NotEmptyValidator(localized));

    editor.setInputTransformer(transformer.createTransformer(title));
  }

  @Override
  protected void onRefresh(Task model) {
    projects.clear();
    contexts.clear();

    MultiQueyBuilder<Task> projectQuery = index.multiQuery(Task.class);
    projectQuery.query(StandardQueries.stateQuery(), s -> s != TaskState.UNPROCESSED);
    projectQuery.query(StandardQueries.finishedQuery(), s -> !s);
    projects.addAll(projectQuery.find().stream().map(Task::getTitle).sorted().collect(Collectors.toList()));

    MultiQueyBuilder<Task> contextQuery = index.multiQuery(Task.class);
    contextQuery.query(StandardQueries.stateQuery(), s -> s != TaskState.UNPROCESSED);
    contextQuery.query(StandardQueries.contextQuery(), Objects::nonNull);
    contexts.addAll(contextQuery.find().stream().map(Task::getContext).distinct().sorted().collect(Collectors.toList()));

    tagsController.getSelectedTags().clear();
    tagsController.getSelectedTags().addAll(model.getHeader().getTags());

    if (model.getState() == TaskState.UNPROCESSED) {
      state.getSelectionModel().select(0);
    } else {
      state.getSelectionModel().select(model.getState().name());
    }
    parent.reload();
    context.reload();
  }

  @Override
  public void duringSave(Task model) {
    LinkedHashSet<String> tags = new LinkedHashSet<>(tagsController.getSelectedTags());
    model.getHeader().setTags(tags);
    TaskState taskState = TaskState.valueOf(state.getValue());
    model.setState(taskState);

    String item = parent.getItem();
    if (item != null && !item.trim().isEmpty()) {
      Set<Task> parents = index.query(AdocFile.class, StandardQueries.titleQuery(), title1 -> title1.equals(item)).stream()//
        .filter(f -> f instanceof Task)//
        .map(f -> (Task) f)//
        .collect(Collectors.toSet());
      if (parents.size() == 1) {
        Task parent = parents.iterator().next();
        model.setParent(parent.getPath().getParent());
      }
    }
  }

  private List<String> getContexts(String s) {
    return contexts.stream().filter(p -> p.toLowerCase(Locale.ROOT).trim().startsWith(s.toLowerCase(Locale.ROOT).trim())).sorted().collect(Collectors.toList());
  }

  private List<String> getParents(String s) {
    return projects.stream().filter(p -> p.toLowerCase(Locale.ROOT).trim().startsWith(s.toLowerCase(Locale.ROOT).trim())).sorted().collect(Collectors.toList());
  }
}
