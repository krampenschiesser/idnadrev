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
package de.ks.idnadrev.information.view.selection.chart;

import de.ks.idnadrev.entity.information.ChartInfo;
import de.ks.idnadrev.entity.information.Information;
import de.ks.idnadrev.information.view.BaseInformationOverviewController;
import de.ks.idnadrev.information.view.InformationPreviewItem;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;

import java.net.URL;
import java.util.ResourceBundle;

public class ChartSelectionController extends BaseInformationOverviewController {
  @FXML
  Button select;
  private Information<?> selectedItem;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    TableView<InformationPreviewItem> informationList = listController.getInformationList();
    informationList.setOnKeyReleased(e -> {
      if (e.getCode() == KeyCode.ENTER) {
        if (currentPreview != null) {
          onSelect();
        }
      }
    });
    informationList.setOnMouseClicked(e -> {
      if (e.getClickCount() > 1) {
        if (currentPreview != null) {
          onSelect();
        }
      }
    });

    listController.setFixedTypeFilter(ChartInfo.class);
  }

  @FXML
  public void onSelect() {
    selectedItem = currentPreview.getCurrentItem();
    controller.stopCurrent();
  }

  public ChartInfo getSelectedItem() {
    return (ChartInfo) selectedItem;
  }
}
