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
package de.ks.text.image;

import de.ks.activity.ActivityController;
import de.ks.imagecache.Images;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Dimension2D;
import javafx.geometry.HPos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class SelectImageController implements Initializable {
  private static final Logger log = LoggerFactory.getLogger(SelectImageController.class);
  @FXML
  private FlowPane imagePane;
  @Inject
  ActivityController activityController;
  protected final SimpleStringProperty selectedImagePath = new SimpleStringProperty();
  protected final Map<String, GridPane> image2Grid = new HashMap<>();
  protected final List<Future<?>> loadingFutures = new ArrayList<>();

  protected Dimension2D defaultSize = new Dimension2D(200, 200);

  public Future<?> addImage(String name, String path) {
    CompletableFuture<Image> future = Images.later(path, activityController.getExecutorService());
    future.thenAcceptAsync(img -> this.addImageToPane(img, name, path), activityController.getJavaFXExecutor());
    this.loadingFutures.add(future);
    return future;
  }

  private void addImageToPane(Image image, String name, String path) {
    ImageView view = new ImageView(image);
    view.setPreserveRatio(true);
    view.setFitHeight(defaultSize.getHeight());
    view.setFitWidth(defaultSize.getWidth());
    view.setCursor(Cursor.HAND);
    Button btn = new Button("", view);
    btn.setOnAction(e -> {
      selectedImagePath.set(null);
      selectedImagePath.set(path);
    });
    btn.setPrefSize(defaultSize.getWidth(), defaultSize.getHeight());

    GridPane grid = new GridPane();
    grid.add(btn, 0, 0);
    grid.add(new Label(name), 0, 1);
    grid.setPrefHeight(Control.USE_COMPUTED_SIZE);
    grid.setPrefWidth(Control.USE_COMPUTED_SIZE);
    ColumnConstraints constraints = new ColumnConstraints();
    constraints.setHalignment(HPos.CENTER);
    grid.getColumnConstraints().add(constraints);
    grid.setMaxSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);

    image2Grid.put(name, grid);
    log.debug("Added image {} to pane {}", name, System.identityHashCode(imagePane));

    imagePane.getChildren().add(grid);
  }

  public void removeImage(String name) {
    imagePane.getChildren().remove(image2Grid.get(name));
  }

  public Dimension2D getDefaultSize() {
    return defaultSize;
  }

  public void setDefaultSize(Dimension2D defaultSize) {
    this.defaultSize = defaultSize;
  }

  public String getSelectedImagePath() {
    return selectedImagePath.get();
  }

  public SimpleStringProperty selectedImagePathProperty() {
    return selectedImagePath;
  }

  public void setSelectedImagePath(String selectedImagePath) {
    this.selectedImagePath.set(selectedImagePath);
  }

  public FlowPane getImagePane() {
    return imagePane;
  }

  public List<Future<?>> getLoadingFutures() {
    return loadingFutures;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    imagePane.setPrefSize(800, 600);
  }
}
