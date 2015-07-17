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
package de.ks.gallery.ui.thumbnail;

import de.ks.gallery.GalleryItem;
import de.ks.gallery.ui.slideshow.Slideshow;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.image.ImageView;

public class Thumbnail {
  public static final int DEFAULT_WIDTH = 350;

  private final Button button;
  private final ImageView imageView;

  protected SimpleObjectProperty<GalleryItem> item = new SimpleObjectProperty<>();
  protected SimpleObjectProperty<Slideshow> slideshow = new SimpleObjectProperty<>();

  public Thumbnail() {
    button = new Button();

    button.getStyleClass().add("thumbnail");
    button.setContentDisplay(ContentDisplay.TOP);
    imageView = new ImageView();
    imageView.setFitWidth(DEFAULT_WIDTH);
    imageView.setFitHeight(350);
    button.setGraphic(imageView);
    item.addListener((p, o, n) -> {
      if (n != null) {
        button.setText(n.getName());
        imageView.setImage(null);
      } else {
        button.setText("");
        imageView.setImage(null);
      }
    });
    imageView.setOnMouseClicked(e -> openFullScreen());
    button.setOnAction(e -> openFullScreen());
  }

  private void openFullScreen() {
    if (item.get() == null) {
      return;
    }
    if (slideshow.get() == null) {
      return;
    }
    slideshow.get().show(item.get());
  }

  public GalleryItem getItem() {
    return item.get();
  }

  public SimpleObjectProperty<GalleryItem> itemProperty() {
    return item;
  }

  public void setItem(GalleryItem item) {
    this.item.set(item);
  }

  public Control getRoot() {
    return button;
  }

  public Slideshow getSlideshow() {
    return slideshow.get();
  }

  public SimpleObjectProperty<Slideshow> slideshowProperty() {
    return slideshow;
  }

  public void setSlideshow(Slideshow slideshow) {
    this.slideshow.set(slideshow);
  }

  public void reset() {
    imageView.setFitWidth(Thumbnail.DEFAULT_WIDTH);
    imageView.setFitHeight(Thumbnail.DEFAULT_WIDTH);
    imageView.setImage(null);
  }

  public ImageView getImageView() {
    return imageView;
  }
}
