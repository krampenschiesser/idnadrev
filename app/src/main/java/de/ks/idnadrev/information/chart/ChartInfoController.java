/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
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

package de.ks.idnadrev.information.chart;

import de.ks.BaseController;
import de.ks.idnadrev.entity.information.ChartInfo;
import de.ks.idnadrev.entity.information.TextInfo;
import de.ks.idnadrev.entity.information.UmlDiagramInfo;
import de.ks.validation.validators.NamedEntityMustNotExistValidator;
import de.ks.validation.validators.NotEmptyValidator;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ChartInfoController extends BaseController<ChartInfo> {
  @FXML
  protected Button saveImage;
  @FXML
  protected Button fullscreen;
  @FXML
  protected StackPane previewContainer;
  @FXML
  protected TextField name;

  @FXML
  protected SplitPane splitPane;
  //  @FXML
//  protected TextArea content;
  @FXML
  protected StackPane contentContainer;
  @FXML
  protected Button saveBtn;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    StringProperty nameProperty = store.getBinding().getStringProperty(UmlDiagramInfo.class, t -> t.getName());
    name.textProperty().bindBidirectional(nameProperty);

//    contentContainer.getChildren().add(spreadsheetView);
//FIXME create editable table, controlsfx spreadsheet doesn't work...
//    StringProperty contentProperty = store.getBinding().getStringProperty(UmlDiagramInfo.class, t -> t.getContent());
//    content.textProperty().bindBidirectional(contentProperty);

    validationRegistry.registerValidator(name, new NotEmptyValidator());
    validationRegistry.registerValidator(name, new NamedEntityMustNotExistValidator<>(UmlDiagramInfo.class, t -> t.getId() == store.<TextInfo>getModel().getId()));

    saveBtn.disableProperty().bind(validationRegistry.invalidProperty());

  }

  @FXML
  protected void onSave() {
    controller.save();
    controller.stopCurrent();
  }

  @FXML
  protected void onSaveImage() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setInitialFileName("umlDiagram");
    FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("png", "png");
    fileChooser.setSelectedExtensionFilter(filter);

    File file = fileChooser.showOpenDialog(saveBtn.getScene().getWindow());
    if (file != null) {
    }
  }

  @FXML
  public void onShowFullScreen() {

  }
}
