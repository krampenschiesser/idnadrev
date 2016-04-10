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
import de.ks.standbein.BaseController;
import de.ks.standbein.imagecache.Images;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
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
  private StackPane crudContainer;
  @FXML
  private StackPane tableContainer;
  @FXML
  private StackPane previewContainer;

  private CRUDController crud;
  private ThoughtTable thoughtTableController;
  private ThoughtPreview thoughtPreviewController;

  private Button toTask = new Button();
  private Button toDocument = new Button();

  @Inject
  Images images;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    CRUDController.load(activityInitialization, v -> crudContainer.getChildren().add(v), ctrl -> this.crud = ctrl);
    activityInitialization.loadAdditionalController(ThoughtTable.class, v -> tableContainer.getChildren().add(v), ctrl -> this.thoughtTableController = ctrl);
    activityInitialization.loadAdditionalController(ThoughtPreview.class, v -> previewContainer.getChildren().add(v), ctrl -> this.thoughtPreviewController = ctrl);

    BooleanBinding itemSelected = thoughtTableController.getThoughtTable().getSelectionModel().selectedItemProperty().isNull();
    crud.getDeleteButton().disableProperty().bind(itemSelected);
    crud.getCenterButtonContainer().getChildren().addAll(toTask, toDocument);

    toTask.setText(localized.get("toTask"));
    toTask.setGraphic(createImageView("toTask.png"));
    toTask.disableProperty().bind(itemSelected);
    toDocument.setText(localized.get("toDocument"));
    toDocument.setGraphic(createImageView("toDocument.png"));
    toDocument.disableProperty().bind(itemSelected);

  }

  protected ImageView createImageView(String imagePath) {
    ImageView imageView = new ImageView(images.get(imagePath));
    imageView.setFitHeight(32);
    imageView.setFitWidth(32);
    return imageView;
  }
}
