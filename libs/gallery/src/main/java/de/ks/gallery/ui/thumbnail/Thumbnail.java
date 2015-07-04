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
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Thumbnail {
  private static final Logger log = LoggerFactory.getLogger(Thumbnail.class);

  private final Button button;
  private final ImageView imageView;

  protected SimpleObjectProperty<GalleryItem> item = new SimpleObjectProperty<>();
  protected SimpleObjectProperty<Slideshow> slideshow = new SimpleObjectProperty<>();
  private Stage fullscreenStage;

  public Thumbnail() {
    button = new Button();
    button.getStyleClass().add("thumbnail");
    button.setContentDisplay(ContentDisplay.TOP);
    imageView = new ImageView();
    button.setGraphic(imageView);
    item.addListener((p, o, n) -> {
      if (n != null) {

        button.setText(n.getName());
        Image image = n.getThumbNail();
        imageView.setImage(image);
        imageView.setFitHeight(image.getHeight());
        imageView.setFitWidth(image.getWidth());
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

  public Node getRoot() {
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
}
