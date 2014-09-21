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
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;

public class ChartInfoController extends BaseController<ChartInfo> {
  private static final Logger log = LoggerFactory.getLogger(ChartInfoController.class);
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

  private XYChart<String, Number> xyChart;
  private PieChart pieChart;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    StringProperty nameProperty = store.getBinding().getStringProperty(UmlDiagramInfo.class, t -> t.getName());
    name.textProperty().bindBidirectional(nameProperty);

    chartType.setItems(FXCollections.observableArrayList(ChartType.values()));
    chartType.getItems().remove(ChartType.BUBBLE);
    chartType.valueProperty().addListener((p, o, n) -> {
      initializePreviewChart(n);
    });
    chartType.setValue(ChartType.LINE);

    validationRegistry.registerValidator(name, new NotEmptyValidator());
    validationRegistry.registerValidator(name, new NamedEntityMustNotExistValidator<>(UmlDiagramInfo.class, t -> t.getId() == store.<TextInfo>getModel().getId()));

    saveBtn.disableProperty().bind(validationRegistry.invalidProperty());
    editorController.setCallback(this::recompute);

    previewContainer.getStyleClass().add("fullScreenBg");
  }

  protected void initializePreviewChart(ChartType chartType) {
    previewContainer.getChildren().clear();
    pieChart = null;
    xyChart = null;

    if (chartType == ChartType.PIE) {
      this.pieChart = new PieChart();
      previewContainer.getChildren().add(pieChart);
      recompute(editorController.getData());
    } else {
      Supplier<XYChart<String, Number>> supplier = getXYChartSupplier(chartType);
      xyChart = supplier.get();
      previewContainer.getChildren().add(xyChart);
      recompute(editorController.getData());
    }
  }

  private Supplier<XYChart<String, Number>> getXYChartSupplier(ChartType chartType) {
    Supplier<XYChart<String, Number>> supplier = null;
    final CategoryAxis categoryAxis = new CategoryAxis();
    final NumberAxis numberAxis = new NumberAxis();
    numberAxis.setAnimated(true);
    numberAxis.setAutoRanging(true);

    if (chartType == ChartType.LINE) {
      supplier = () -> new LineChart<String, Number>(categoryAxis, numberAxis);
    } else if (chartType == ChartType.AREA) {
      supplier = () -> new AreaChart<String, Number>(categoryAxis, numberAxis);
    } else if (chartType == ChartType.BAR) {
      supplier = () -> new BarChart<String, Number>(categoryAxis, numberAxis);
    } else if (chartType == ChartType.BUBBLE) {
      supplier = () -> new BubbleChart<String, Number>(categoryAxis, numberAxis);
    } else if (chartType == ChartType.SCATTER) {
      supplier = () -> new ScatterChart<String, Number>(categoryAxis, numberAxis);
    } else if (chartType == ChartType.STACKEDBAR) {
      supplier = () -> new StackedBarChart<String, Number>(categoryAxis, numberAxis);
    } else if (chartType == ChartType.STACKEDAREA) {
      supplier = () -> new StackedAreaChart<String, Number>(categoryAxis, numberAxis);
    }
    return supplier;
  }

  private void recompute(ChartPreviewData data) {
    if ((xyChart == null && pieChart == null) || data.getSeries().isEmpty()) {
      return;
    }
    if (pieChart != null) {
      createPieChart(data);
    } else if (xyChart != null) {
      createXYChart(data);
    }
  }

  private void createPieChart(ChartPreviewData data) {
    ChartPreviewData.DataSeries series = data.getSeries().get(0);

    List<PieChart.Data> pieChartSeries = new LinkedList<PieChart.Data>();
    for (int i = 0; i < series.getValues().size(); i++) {
      Double value = series.getValues().get(i);
      String category = data.getCategories().get(i);
      if (category != null) {
        pieChartSeries.add(new PieChart.Data(category, value));
      }
    }
    pieChart.setData(FXCollections.observableList(pieChartSeries));
  }

  private void createXYChart(ChartPreviewData data) {
    List<XYChart.Series<String, Number>> allSeries = new ArrayList<>(data.getSeries().size());

    for (ChartPreviewData.DataSeries dataSeries : data.getSeries()) {
      XYChart.Series<String, Number> series = new XYChart.Series<>();
      series.setName(dataSeries.getTitle());

      List<XYChart.Data<String, Number>> datas = new LinkedList<XYChart.Data<String, Number>>();
      for (int i = 0; i < dataSeries.getValues().size(); i++) {
        Double value = dataSeries.getValues().get(i);
        String category = data.getCategories().get(i);

        datas.add(new XYChart.Data<String, Number>(category, value));
      }
      series.setData(FXCollections.observableList(datas));
      allSeries.add(series);
    }
    xyChart.setData(FXCollections.observableList(allSeries));
    xyChart.getXAxis().setLabel(data.getxAxisTitle());
    xyChart.getYAxis().setLabel(data.getyAxisTitle());
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
    fileChooser.setInitialFileName("chart");
    FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("png", "png");
    fileChooser.setSelectedExtensionFilter(filter);

    File file = fileChooser.showOpenDialog(saveBtn.getScene().getWindow());
    if (file != null) {
      Node pane = createFullScreenPopup(Screen.getPrimary()).getContent().get(0);

      WritableImage image = pane.snapshot(null, null);

      BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null); // Get buffered bufferedImage.
      try {
        ImageIO.write(bufferedImage, "png", file);
      } catch (IOException e) {
        log.error("Could not write image {}", file, e);
      }
    }
  }

  @FXML
  public void onShowFullScreen() {
    if (pieChart == null && xyChart == null) {
      return;
    }
    ObservableList<Screen> screens = Screen.getScreens();
    Screen screen;
    if (screens.size() == 1) {
      screen = screens.get(0);
    } else {
      Optional<Screen> other = screens.stream().filter(s -> !s.equals(Screen.getPrimary())).findFirst();
      screen = other.get();
    }
    Popup popup = createFullScreenPopup(screen);
    popup.show(fullscreen.getScene().getWindow());
  }

  private Popup createFullScreenPopup(Screen screen) {
    StackPane pane = getFullScreenPane(screen);

    Rectangle2D visualBounds = screen.getVisualBounds();
    Popup popup = new Popup();
    popup.getContent().add(pane);
    popup.setX(visualBounds.getMinX());
    popup.setY(visualBounds.getMinY());
    popup.setWidth(visualBounds.getWidth());
    popup.setHeight(visualBounds.getHeight());
    return popup;
  }

  protected StackPane getFullScreenPane(Screen screen) {
    Rectangle2D visualBounds = screen.getVisualBounds();
    Chart fullScreenChart = null;
    if (pieChart != null) {
      fullScreenChart = new PieChart(pieChart.getData());
    } else if (xyChart != null) {
      Supplier<XYChart<String, Number>> xyChartSupplier = getXYChartSupplier(chartType.getValue());
      XYChart<String, Number> numberXYChart = xyChartSupplier.get();
      numberXYChart.setData(xyChart.getData());
      Axis<Number> numberAxis = numberXYChart.getYAxis();
      numberAxis.setAnimated(false);
      numberAxis.setAutoRanging(true);

      fullScreenChart = numberXYChart;
    }

    StackPane pane = new StackPane();
    pane.getStyleClass().add("fullScreenBg");
    pane.setPrefWidth(visualBounds.getWidth());
    pane.setPrefHeight(visualBounds.getHeight());
    pane.getChildren().add(fullScreenChart);
    return pane;
  }
}
