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
package de.ks.idnadrev.thought.view;

import de.ks.idnadrev.task.Task;
import de.ks.idnadrev.ui.CRUDController;
import de.ks.idnadrev.util.ButtonHelper;
import de.ks.standbein.BaseController;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ViewThoughtsController extends BaseController<List<Task>> {

  @FXML
  private VBox root;
  @FXML
  private SplitPane split;
  @FXML
  private StackPane crudContainer;
  @FXML
  private StackPane tableContainer;
  @FXML
  private StackPane previewContainer;

  private CRUDController crud;
  private ThoughtTable thoughtTableController;
  private ThoughtPreview thoughtPreviewController;

  private Button toTask;
  private Button toDocument;

  @Inject
  ButtonHelper buttonHelper;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    CRUDController.load(activityInitialization, v -> crudContainer.getChildren().add(v), ctrl -> this.crud = ctrl);
    activityInitialization.loadAdditionalController(ThoughtTable.class, v -> tableContainer.getChildren().add(v), ctrl -> this.thoughtTableController = ctrl);
    activityInitialization.loadAdditionalController(ThoughtPreview.class, v -> previewContainer.getChildren().add(v), ctrl -> this.thoughtPreviewController = ctrl);

    BooleanBinding itemSelected = thoughtTableController.getThoughtTable().getSelectionModel().selectedItemProperty().isNull();
    crud.getDeleteButton().disableProperty().bind(itemSelected);

    toTask = buttonHelper.createImageButton("toTask", "toTask.png", 24);
    toTask.disableProperty().bind(itemSelected);
    toDocument = buttonHelper.createImageButton("toDocument", "toDocument.png", 24);
    toDocument.setContentDisplay(ContentDisplay.RIGHT);
    toDocument.disableProperty().bind(itemSelected);
    crud.getCenterButtonContainer().getChildren().addAll(toTask, toDocument);
  }
}
