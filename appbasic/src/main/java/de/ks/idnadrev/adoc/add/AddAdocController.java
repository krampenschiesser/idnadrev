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

package de.ks.idnadrev.adoc.add;

import de.ks.idnadrev.adoc.AdocFile;
import de.ks.idnadrev.adoc.ui.TagSelection;
import de.ks.idnadrev.crud.CRUDController;
import de.ks.idnadrev.repository.RepositoryService;
import de.ks.idnadrev.repository.ui.ActiveRepositoryController;
import de.ks.idnadrev.util.AdocTransformer;
import de.ks.standbein.BaseController;
import de.ks.standbein.validation.validators.NotEmptyValidator;
import de.ks.texteditor.TextEditor;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.ResourceBundle;

public class AddAdocController extends BaseController<AdocFile> {
  @FXML
  protected VBox root;
  @FXML
  protected GridPane editorGrid;
  @FXML
  private TextField title;
  @FXML
  private TagSelection tagsController;
  @FXML
  private StackPane editorContainer;
  @FXML
  private CRUDController crudController;
  @FXML
  private ActiveRepositoryController repositoryController;

  private TextEditor editor;

  @Inject
  RepositoryService repositoryService;
  @Inject
  AdocTransformer transformer;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    TextEditor.load(activityInitialization, editorContainer.getChildren()::add, ctrl -> editor = ctrl);
    Button saveButton = crudController.getSaveButton();
    saveButton.setVisible(true);
    saveButton.setOnAction(e -> {
      controller.save();
      AddAdocDs datasource = (AddAdocDs) store.getDatasource();
      if (datasource.hasHint()) {
        controller.stopCurrent();
      } else {
        controller.reload();
      }
    });
    tagsController.setEditable(true);

    editor.textProperty().bindBidirectional(store.getBinding().getStringProperty(AdocFile.class, AdocFile::getContent));
    title.textProperty().bindBidirectional(store.getBinding().getStringProperty(AdocFile.class, t -> t.getHeader().getTitle()));

    validationRegistry.registerValidator(title, new NotEmptyValidator(localized));

    editor.setInputTransformer(transformer.createTransformer(title));
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
  }

  @Override
  protected void onRefresh(AdocFile model) {
    tagsController.getSelectedTags().clear();
    tagsController.getSelectedTags().addAll(model.getHeader().getTags());
  }

  @Override
  public void duringSave(AdocFile model) {
    LinkedHashSet<String> tags = new LinkedHashSet<>(tagsController.getSelectedTags());
    model.getHeader().setTags(tags);
  }
}
