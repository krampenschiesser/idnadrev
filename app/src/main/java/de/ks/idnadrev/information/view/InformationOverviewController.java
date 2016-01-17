/**
 * Copyright [2014] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.idnadrev.information.view;

import de.ks.idnadrev.entity.information.Information;
import de.ks.idnadrev.information.view.preview.TextInfoPreview;
import de.ks.standbein.BaseController;
import de.ks.standbein.application.fxml.DefaultLoader;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class InformationOverviewController extends BaseController<List<Information>> {
  @FXML
  protected Button edit;
  @FXML
  protected InformationListView listController;
  @FXML
  protected StackPane previewContainer;
  protected TextInfoPreview preview;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    DefaultLoader<Node, TextInfoPreview> loader = activityInitialization.loadAdditionalController(TextInfoPreview.class);
    preview = loader.getController();
    previewContainer.getChildren().add(loader.getView());

    TableView<Information> informationList = listController.getInformationList();
    informationList.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
      if (n == null) {
        preview.show(null);
      } else {
        preview.show(n);
      }
    });
    informationList.setOnKeyReleased(e -> {
      if (e.getCode() == KeyCode.ENTER) {
        preview.edit();
      }
    });
    informationList.setOnMouseClicked(e -> {
      if (e.getClickCount() > 1) {
        preview.edit();
      }
    });
    edit.disableProperty().bind(informationList.getSelectionModel().selectedItemProperty().isNull());
  }

  @FXML
  public void onEdit() {
    preview.edit();
  }
}
