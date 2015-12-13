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

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class BaseInformationOverviewController extends BaseController<List<InformationPreviewItem>> {
  @FXML
  protected InformationListView listController;
  @FXML
  protected StackPane previewContainer;
  protected InformationPreview<?> currentPreview;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    TableView<InformationPreviewItem> informationList = listController.getInformationList();
    ReadOnlyObjectProperty<InformationPreviewItem> selectedItemProperty = informationList.getSelectionModel().selectedItemProperty();
    selectedItemProperty.addListener((p, o, n) -> {
      previewContainer.getChildren().clear();
      if (n != null) {
        if (n.getType().equals(TextInfo.class)) {
          TextInfoPreview preview = activityInitialization.getControllerInstance(TextInfoPreview.class);
          currentPreview = preview;
          previewContainer.getChildren().add(preview.show(n));
        }
        if (n.getType().equals(UmlDiagramInfo.class)) {
          UmlPreview preview = activityInitialization.getControllerInstance(UmlPreview.class);
          currentPreview = preview;
          previewContainer.getChildren().add(preview.show(n));
        }
        if (n.getType().equals(ChartInfo.class)) {
          ChartPreview preview = activityInitialization.getControllerInstance(ChartPreview.class);
          currentPreview = preview;
          previewContainer.getChildren().add(preview.show(n));
        }
      }
    });
  }
}
