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
package de.ks.idnadrev.thought.add;

import de.ks.idnadrev.repository.Repository;
import de.ks.idnadrev.repository.RepositoryService;
import de.ks.idnadrev.task.Task;
import de.ks.idnadrev.ui.CRUDController;
import de.ks.idnadrev.util.NamedConverter;
import de.ks.standbein.BaseController;
import de.ks.standbein.validation.validators.NotEmptyValidator;
import de.ks.texteditor.TextEditor;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class AddThoughtController extends BaseController<Task> {
  @FXML
  protected VBox root;
  @FXML
  protected GridPane editorGrid;
  @FXML
  private TextField title;
  @FXML
  private ComboBox<Repository> repository;
  @FXML
  private TextField tags;
  @FXML
  private StackPane editorContainer;
  @FXML
  private CRUDController crudController;

  private TextEditor editor;

  @Inject
  RepositoryService repositoryService;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    TextEditor.load(activityInitialization, editorContainer.getChildren()::add, ctrl -> editor = ctrl);
    crudController.getSaveButton().setVisible(true);

    editor.textProperty().bindBidirectional(store.getBinding().getStringProperty(Task.class, Task::getContent));

    validationRegistry.registerValidator(title, new NotEmptyValidator(localized));
    repository.setConverter(new NamedConverter<>(name -> repositoryService.getRepositories().stream().filter(r -> r.getName().equals(name)).findAny().get()));
  }

  @Override
  public void onStart() {
    onResume();
  }

  @Override
  public void onResume() {
    if (!editor.getCodeArea().isFocused()) {
      controller.getExecutorService().submit(() -> {
        try {
          Thread.sleep(100);
          controller.getJavaFXExecutor().submit(() -> title.requestFocus());
        } catch (InterruptedException e) {
          //ok
        }
      });
    }
    controller.getJavaFXExecutor().submit(() -> {
      ObservableList<Repository> items = repository.getItems();
      items.clear();
      items.addAll(repositoryService.getRepositories());
      if (repository.getSelectionModel().isEmpty() && !items.isEmpty()) {
        repository.getSelectionModel().select(0);
      } else if (!items.contains(repository.getSelectionModel().getSelectedItem())) {
        repository.getSelectionModel().select(0);
      }
    });
  }
}
