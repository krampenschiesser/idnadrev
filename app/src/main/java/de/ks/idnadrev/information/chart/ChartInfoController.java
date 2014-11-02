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
import de.ks.activity.initialization.LoadInFXThread;
import de.ks.i18n.Localized;
import de.ks.idnadrev.entity.information.*;
import de.ks.validation.validators.NamedEntityMustNotExistValidator;
import de.ks.validation.validators.NotEmptyValidator;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.chart.Chart;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Screen;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

@LoadInFXThread
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
  protected TextField yaxisTitle;
  @FXML
  protected Button addColumn;

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

  protected final ChartPreview previewHelper = new ChartPreview(this);
  protected final ChartPreview fullScreenPreviewHelper = new ChartPreview(null);

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    StringProperty nameProperty = store.getBinding().getStringProperty(UmlDiagramInfo.class, t -> t.getName());
    name.textProperty().bindBidirectional(nameProperty);

    chartType.setItems(FXCollections.observableArrayList(ChartType.values()));
    chartType.getItems().remove(ChartType.BUBBLE);
    chartType.valueProperty().addListener((p, o, n) -> {
      ChartInfo model = store.getModel();
      if (model != null) {
        ChartData data = editorController.getData();
        model.setChartType(n);
        model.setChartData(data);
        initializePreviewChart(model);
      }
    });

    validationRegistry.registerValidator(name, new NotEmptyValidator());
    validationRegistry.registerValidator(name, new NamedEntityMustNotExistValidator<>(UmlDiagramInfo.class, t -> t.getId() == store.<TextInfo>getModel().getId()));

    saveBtn.disableProperty().bind(validationRegistry.invalidProperty());
    editorController.setCallback(previewHelper::recompute);

    previewContainer.getStyleClass().add("fullScreenBg");
  }

  protected void initializePreviewChart(ChartInfo chartInfo) {
    previewContainer.getChildren().clear();
    if (chartInfo != null) {
      Chart chart = previewHelper.createNewChart(chartInfo);
      previewContainer.getChildren().add(chart);
    }
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

  protected Popup createFullScreenPopup(Screen screen) {
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
    ChartInfo model = store.getModel();
    model.setChartData(editorController.getData());
    model.setChartType(chartType.getValue());
    Chart fullScreenChart = fullScreenPreviewHelper.createNewChart(model);

    StackPane pane = new StackPane();
    pane.getStyleClass().add("fullScreenBg");
    pane.setPrefWidth(visualBounds.getWidth());
    pane.setPrefHeight(visualBounds.getHeight());
    pane.getChildren().add(fullScreenChart);
    return pane;
  }

  @FXML
  void onAddColumn() {
    Optional<String> input = Dialogs.create().message(Localized.get("column.title")).showTextInput();
    if (input.isPresent()) {
      editorController.addColumnHeader(input.get());
    }
  }

  @Override
  protected void onRefresh(ChartInfo model) {
    ChartData chartData = model.getChartData();
    if (chartData != null) {
      yaxisTitle.setText(chartData.getYAxisTitle());
    } else {
      yaxisTitle.setText("");
    }

    if (model.getChartData() == null) {
      model.setChartData(editorController.getData());
    } else {
      editorController.setData(model.getChartData());
    }
    controller.getJavaFXExecutor().submit(() -> chartType.getSelectionModel().select(model.getChartType()));
  }

}
