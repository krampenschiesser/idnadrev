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
import de.ks.javafx.ScreenResolver;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Thumbnail {
  private static final Logger log = LoggerFactory.getLogger(Thumbnail.class);

  private final Button button;
  private final ImageView imageView;

  protected SimpleObjectProperty<GalleryItem> item = new SimpleObjectProperty<>();
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

    if (fullscreenStage == null) {
      fullscreenStage = new Stage();
      fullscreenStage.setFullScreen(true);
      fullscreenStage.setFullScreenExitHint("");
      fullscreenStage.setTitle(item.get().getName());
      Rectangle2D bounds = new ScreenResolver().getScreenToShow().getBounds();

      Image image = item.get().getImage();
      ImageView imageView = new ImageView(image);

      StackPane root = new StackPane(imageView);
      root.setStyle("-fx-background-color: black;");

      if (bounds.getWidth() > bounds.getHeight()) {
        imageView.fitHeightProperty().bind(Bindings.min(image.getHeight(), root.heightProperty()));
      } else {
        imageView.fitWidthProperty().bind(Bindings.min(image.getWidth(), root.widthProperty()));
      }
      Scene scene = new Scene(root);
      scene.setOnKeyReleased(e -> {
        if (e.getCode() == KeyCode.ESCAPE) {
          fullscreenStage.close();
        }
      });
      fullscreenStage.setScene(scene);

      fullscreenStage.setX(bounds.getMinX());
      fullscreenStage.setY(bounds.getMinY());
      fullscreenStage.setWidth(bounds.getWidth());
      fullscreenStage.setHeight(bounds.getHeight());

      fullscreenStage.initModality(Modality.NONE);
      fullscreenStage.setOnCloseRequest(e -> this.fullscreenStage = null);
      fullscreenStage.setOnHiding(e -> this.fullscreenStage = null);
      fullscreenStage.show();
    } else {
      StackPane root = (StackPane) fullscreenStage.getScene().getRoot();
      ImageView view = (ImageView) root.getChildren().iterator().next();
      view.setImage(item.get().getImage());
      fullscreenStage.show();
    }
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
}
