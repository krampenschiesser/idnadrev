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

import de.ks.idnadrev.adoc.ui.TagSelection;
import de.ks.idnadrev.index.Index;
import de.ks.idnadrev.index.MultiQueyBuilder;
import de.ks.idnadrev.index.StandardQueries;
import de.ks.idnadrev.repository.ui.ActiveRepositoryController;
import de.ks.idnadrev.task.Task;
import de.ks.idnadrev.task.TaskState;
import de.ks.standbein.BaseController;
import de.ks.standbein.autocomp.AutoCompletionTextField;
import de.ks.texteditor.TextEditor;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import javax.inject.Inject;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AddTaskController extends BaseController<Task> {
  @FXML
  ComboBox state;
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

  @Inject
  AutoCompletionTextField context;
  @Inject
  AutoCompletionTextField parent;
  private TextEditor editor;

  @Inject
  Index index;

  private final List<String> projects = new ArrayList<>();
  private final List<String> contexts = new ArrayList<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    TextEditor.load(activityInitialization, editorContainer.getChildren()::add, ctrl -> editor = ctrl);

    context.setEditable(true);
    context.configure(this::getContexts);
    parent.configure(this::getParents);

    contextContainer.getChildren().add(context.getTextField());
    parentContainer.getChildren().add(parent.getTextField());


    state.getItems().addAll(Stream.of(TaskState.values()).filter(s -> s != TaskState.UNPROCESSED).map(TaskState::name).collect(Collectors.toList()));
    state.getSelectionModel().select(0);
  }

  @Override
  protected void onRefresh(Task model) {
    super.onRefresh(model);

    projects.clear();
    contexts.clear();

    MultiQueyBuilder<Task> projectQuery = index.multiQuery(Task.class);
    projectQuery.query(StandardQueries.stateQuery(), s -> s != TaskState.UNPROCESSED);
    projectQuery.query(StandardQueries.finishedQuery(), s -> !s);
    projects.addAll(projectQuery.find().stream().map(Task::getTitle).sorted().collect(Collectors.toList()));

    MultiQueyBuilder<Task> contextQuery = index.multiQuery(Task.class);
    contexts.addAll(contextQuery.find().stream().map(Task::getContext).distinct().sorted().collect(Collectors.toList()));
  }

  private List<String> getContexts(String s) {
    return contexts.stream().filter(p -> p.toLowerCase(Locale.ROOT).trim().startsWith(s.toLowerCase(Locale.ROOT).trim())).sorted().collect(Collectors.toList());
  }

  private List<String> getParents(String s) {
    return projects.stream().filter(p -> p.toLowerCase(Locale.ROOT).trim().startsWith(s.toLowerCase(Locale.ROOT).trim())).sorted().collect(Collectors.toList());
  }
}
