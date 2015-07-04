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

import de.ks.activity.executor.ActivityExecutor;
import de.ks.gallery.GalleryItem;
import de.ks.javafx.ScreenResolver;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Slideshow {
  private static final Logger log = LoggerFactory.getLogger(Slideshow.class);

  protected ObservableList<GalleryItem> items = FXCollections.observableArrayList();

  protected List<GalleryItem> sorted = new ArrayList<>();

  protected final AtomicInteger currentIndex = new AtomicInteger();
  protected ImageView imageView = new ImageView();
  protected StackPane root = new StackPane(imageView);
  protected Stage fullscreenStage;
  private final Scene scene;

  @Inject
  ActivityExecutor executor;

  public Slideshow() {
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

    fullscreenStage.setTitle(item.getName());
    fullscreenStage.setFullScreen(true);
    fullscreenStage.show();
  }

  public void next() {
    int index = getNextIndex();
    show(index);
    preloadNext();
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
    int index = getPreviousIndex();
    show(index);
    preloadPrevious();
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
    this.items = items;
  }
}
