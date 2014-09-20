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
import de.ks.idnadrev.entity.information.ChartType;
import de.ks.idnadrev.entity.information.TextInfo;
import de.ks.idnadrev.entity.information.UmlDiagramInfo;
import de.ks.validation.validators.NamedEntityMustNotExistValidator;
import de.ks.validation.validators.NotEmptyValidator;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
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
  @FXML
  protected ChartDataEditor editorController;
  @FXML
  protected ScrollPane contentContainer;
  @FXML
  protected Button saveBtn;
  @FXML
  protected ComboBox<ChartType> chartType;

  private LineChart<String, Number> chart;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    StringProperty nameProperty = store.getBinding().getStringProperty(UmlDiagramInfo.class, t -> t.getName());
    name.textProperty().bindBidirectional(nameProperty);

    chartType.setItems(FXCollections.observableArrayList(ChartType.values()));
    chartType.valueProperty().addListener((p, o, n) -> {
      initializePreview(n);
    });
    chartType.setValue(ChartType.LINE);

    validationRegistry.registerValidator(name, new NotEmptyValidator());
    validationRegistry.registerValidator(name, new NamedEntityMustNotExistValidator<>(UmlDiagramInfo.class, t -> t.getId() == store.<TextInfo>getModel().getId()));

    saveBtn.disableProperty().bind(validationRegistry.invalidProperty());
  }

  protected void initializePreview(ChartType chartType) {
    previewContainer.getChildren().clear();
    if (chartType == ChartType.LINE) {
      chart = new LineChart<>(new CategoryAxis(), new NumberAxis());
      previewContainer.getChildren().add(chart);
      recompute();
    }
  }

  private void recompute() {
    if (chart == null) {
      return;
    }
//    ObservableList<TableColumn<ChartRow, ?>> columns = table.getColumns();
//
//    List<XYChart.Series<String, Number>> allSeries = new ArrayList<>(columns.size());
//    for (int i = 1; i < columns.size(); i++) {
//      TableColumn<ChartRow, ?> column = columns.get(i);
//      XYChart.Series<String, Number> series = new LineChart.Series<>();
//      series.setName(column.getText());
//
//      List<XYChart.Data<String, Number>> data = new ArrayList<>(items.size());
//      for (ChartRow item : items) {
//        String desc = item.getValue(0);
//        if (desc == null) {
//          continue;
//        }
//        String value = item.getValue(i);
//        if (value != null) {
//          try {
//            Double valueParsed = Double.valueOf(value);
//            data.add(new XYChart.Data<>(desc, valueParsed));
//          } catch (NumberFormatException e) {
//            //
//          }
//        }
//      }
//      series.setData(FXCollections.observableList(data));
//      allSeries.add(series);
//    }
//    chart.setData(FXCollections.observableList(allSeries));
  }

  @FXML
  protected void onSave() {
    if (saveBtn.isFocused()) {
      controller.save();
      controller.stopCurrent();
    }
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
