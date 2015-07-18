/**
 * Copyright [2015] [Christian Loehnert]
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
package de.ks.gallery.ui.slideshow;

import de.ks.BaseController;
import de.ks.activity.executor.ActivityExecutor;
import de.ks.gallery.GalleryItem;
import de.ks.i18n.Localized;
import de.ks.javafx.ScreenResolver;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Slideshow extends BaseController<Object> {
  private static final Logger log = LoggerFactory.getLogger(Slideshow.class);

  @FXML
  private GridPane menuBar;
  @FXML
  private Button markForDeletion;
  @FXML
  private Button markImage;

  @FXML
  private ChoiceBox<Integer> speed;
  @FXML
  private ToggleButton startStop;

  @FXML
  private StackPane root;
  @FXML
  private ImageView imageView;
  @FXML
  private Label imageTitle;

  protected final ObservableList<GalleryItem> items = FXCollections.observableArrayList();

  protected final List<GalleryItem> sorted = new ArrayList<>();

  protected final AtomicInteger currentIndex = new AtomicInteger();
  protected Stage fullscreenStage;
  protected Scene scene;

  protected final ObservableList<GalleryItem> markedForDeletion = FXCollections.observableArrayList();
  protected final ObservableList<GalleryItem> markedItems = FXCollections.observableArrayList();

  @Inject
  ActivityExecutor executor;
  private ScheduledFuture<?> scheduledFuture;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    items.addListener((ListChangeListener<GalleryItem>) c -> {
      sorted.clear();
      sorted.addAll(items);
      Collections.sort(sorted);
    });

    root.setStyle("-fx-background-color: black;");
    root.setOnMouseClicked(e -> {
      if (e.getSceneX() < root.getScene().getWidth() / 2) {
        previous();
      } else if (e.getSceneX() > root.getScene().getWidth() / 2) {
        next();
      }
    });

    controller.getJavaFXExecutor().submit(this::createStage);

    speed.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 7, 8, 10, 15, 20));
    speed.setValue(3);
    speed.valueProperty().addListener((p, o, n) -> restartWithTimeout());

    menuBar.setVisible(false);
    root.setOnMouseMoved(e -> {
      if (e.getSceneY() < 100) {
        menuBar.setVisible(true);
      } else {
        menuBar.setVisible(false);
      }
    });
    String s = getClass().getResource("slideshow.css").toExternalForm();
    root.getStylesheets().add(s);
  }

  protected void createStage() {
    fullscreenStage = new Stage();
    fullscreenStage.setFullScreen(true);
    fullscreenStage.setFullScreenExitHint("");
    scene = new Scene(root);
    scene.setOnKeyReleased(e -> {
      if (e.getCode() == KeyCode.ESCAPE) {
        fullscreenStage.close();
      }
      if (e.getCode() == KeyCode.SPACE) {
        next();
      } else if (e.getCode() == KeyCode.PAGE_DOWN) {
        next();
      } else if (e.getCode() == KeyCode.PAGE_UP) {
        previous();
      } else if (e.getCode() == KeyCode.RIGHT) {
        next();
      } else if (e.getCode() == KeyCode.LEFT) {
        previous();
      } else if (e.getCode() == KeyCode.ALT) {
        menuBar.setVisible(false);
      }

    });
    scene.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ALT) {
        menuBar.setVisible(true);
      }
    });
    scene.setOnScroll(e -> {
      double deltaY = e.getDeltaY();
      if (deltaY > 0) {
        previous();
      } else {
        next();
      }
    });
    fullscreenStage.setScene(scene);

    Rectangle2D bounds = new ScreenResolver().getScreenToShow().getBounds();
    fullscreenStage.setX(bounds.getMinX());
    fullscreenStage.setY(bounds.getMinY());
    fullscreenStage.setWidth(bounds.getWidth());
    fullscreenStage.setHeight(bounds.getHeight());

    fullscreenStage.initModality(Modality.NONE);
    fullscreenStage.setOnHiding(e -> stop());
  }

  public void show(GalleryItem item) {
    show(sorted.indexOf(item));
  }

  public void show(int index) {
    currentIndex.set(index);

    GalleryItem item = sorted.get(index);

    Rectangle2D bounds = new ScreenResolver().getScreenToShow().getBounds();
    Image image = item.getImage();
    if (bounds.getWidth() > bounds.getHeight()) {
      imageView.fitHeightProperty().bind(Bindings.min(image.getHeight(), root.heightProperty()));
    } else {
      imageView.fitWidthProperty().bind(Bindings.min(image.getWidth(), root.widthProperty()));
    }
    imageView.setImage(item.getImage());
    imageTitle.setText(item.getName());

    fullscreenStage.setTitle(item.getName());
    fullscreenStage.setFullScreen(true);
    fullscreenStage.show();
  }

  public void next() {
    clearPrevious();
    int index = getNextIndex();
    show(index);
    preloadNext();
  }

  protected void clearPrevious() {
    if (sorted.size() <= 3) {
      return;
    }
    int old = currentIndex.get() - 1;
    if (old < 0) {
      old = sorted.size() - 1;
    }
    sorted.get(old).clear();
  }

  protected void preloadNext() {
    int next = currentIndex.get() + 1;
    if (next >= sorted.size()) {
      next = 0;
    }
    final int load = next;
    executor.submit(() -> sorted.get(load).getImage());
  }

  protected int getNextIndex() {
    int index = currentIndex.incrementAndGet();
    if (index >= items.size()) {
      index = 0;
      currentIndex.set(0);
    }
    return index;
  }

  public void previous() {
    clearNext();
    int index = getPreviousIndex();
    show(index);
    preloadPrevious();
  }

  protected void clearNext() {
    if (sorted.size() <= 3) {
      return;
    }
    int old = currentIndex.get() + 1;
    if (old >= sorted.size()) {
      old = 0;
    }
    sorted.get(old).clear();
  }

  protected void preloadPrevious() {
    int next = currentIndex.get() - 1;
    if (next < 0) {
      next = sorted.size() - 1;
    }
    final int load = next;
    executor.submit(() -> sorted.get(load).getImage());
  }

  protected int getPreviousIndex() {
    int index = currentIndex.decrementAndGet();
    if (index < 0) {
      currentIndex.set(items.size() - 1);
      index = currentIndex.get();
    }
    return index;
  }

  public ObservableList<GalleryItem> getItems() {
    return items;
  }

  public void setItems(ObservableList<GalleryItem> items) {
    this.items.clear();
    this.items.addAll(items);
  }

  @FXML
  void onMarkImage() {
    GalleryItem item = sorted.get(currentIndex.get());
    if (!markedForDeletion.contains(item)) {
      log.info("Marked item {}", item.getName());
      markedItems.add(item);
    }
  }

  @FXML
  void onMarkForDeletion() {
    GalleryItem item = sorted.get(currentIndex.get());
    if (!markedForDeletion.contains(item)) {
      log.info("Marked item {} for deletion", item.getName());
      markedForDeletion.add(item);
    }
  }

  @FXML
  void onStartStop() {
    if (startStop.isSelected()) {
      start();
    } else {
      stop();
    }
  }

  private void start() {
    startStop.setText(Localized.get("gallery.stop.mneominc"));
    restartWithTimeout();
  }

  private void stop() {
    log.info("Stopping slideshow");
    if (scheduledFuture != null) {
      scheduledFuture.cancel(true);
      scheduledFuture = null;
    }
    startStop.setText(Localized.get("gallery.start.mneominc"));
    startStop.setSelected(false);
  }

  private void restartWithTimeout() {
    if (scheduledFuture != null) {
      scheduledFuture.cancel(true);
      scheduledFuture = null;
    }
    log.info("Starting slideshow with timeout {}s", speed.getValue());
    Integer delay = Integer.valueOf(speed.getValue());
    scheduledFuture = controller.getExecutorService().scheduleAtFixedRate(() -> controller.getJavaFXExecutor().submit(this::next), delay, delay, TimeUnit.SECONDS);
  }

  public Button getMarkImage() {
    return markImage;
  }

  public Button getMarkForDeletion() {
    return markForDeletion;
  }

  public ObservableList<GalleryItem> getMarkedForDeletion() {
    return markedForDeletion;
  }

  public ObservableList<GalleryItem> getMarkedItems() {
    return markedItems;
  }
}
