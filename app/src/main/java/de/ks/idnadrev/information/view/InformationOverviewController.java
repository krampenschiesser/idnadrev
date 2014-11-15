/**
 * Copyright [2014] [Christian Loehnert]
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
package de.ks.idnadrev.information.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;

import java.net.URL;
import java.util.ResourceBundle;

public class InformationOverviewController extends BaseInformationOverviewController {
  @FXML
  protected Button edit;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    TableView<InformationPreviewItem> informationList = listController.getInformationList();
    informationList.setOnKeyReleased(e -> {
      if (e.getCode() == KeyCode.ENTER) {
        if (currentPreview != null) {
          currentPreview.edit();
        }
      }
    });
    informationList.setOnMouseClicked(e -> {
      if (e.getClickCount() > 1) {
        if (currentPreview != null) {
          currentPreview.edit();
        }
      }
    });
    edit.disableProperty().bind(informationList.getSelectionModel().selectedItemProperty().isNull());
  }

  @FXML
  public void onEdit() {
    currentPreview.edit();
  }
}
