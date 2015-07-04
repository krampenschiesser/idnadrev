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
package de.ks.gallery.ui;

import de.ks.gallery.GalleryItem;
import de.ks.javafx.ScreenResolver;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class Thumbnail implements Initializable {
  @FXML
  Label name;
  @FXML
  ImageView imageView;
  @FXML
  GridPane root;

  protected SimpleObjectProperty<GalleryItem> item = new SimpleObjectProperty<>();
  private Stage fullscreenStage;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    item.addListener((p, o, n) -> {
      if (n != null) {
        name.setText(n.getName());
        imageView.setImage(n.getThumbNail());
      } else {
        name.setText("");
        imageView.setImage(null);
      }
    });

    imageView.setOnMouseClicked(e -> openFullScreen());
  }

  private void openFullScreen() {
    if (item.get() == null) {
      return;
    }

    if (fullscreenStage == null) {
      fullscreenStage = new Stage();
      fullscreenStage.setTitle(item.get().getName());
      Scene scene = new Scene(new StackPane(new ImageView(item.get().getImage())));
      scene.setOnKeyReleased(e -> {
        if (e.getCode() == KeyCode.ESCAPE) {
          fullscreenStage.close();
        }
      });
      fullscreenStage.setScene(scene);

      Rectangle2D bounds = new ScreenResolver().getScreenToShow().getBounds();
      fullscreenStage.setX(bounds.getMinX());
      fullscreenStage.setY(bounds.getMinY());
      fullscreenStage.setWidth(bounds.getWidth());
      fullscreenStage.setHeight(bounds.getHeight());

      fullscreenStage.initModality(Modality.NONE);
      fullscreenStage.setOnCloseRequest(e -> this.fullscreenStage = null);
      fullscreenStage.show();
    } else {
      StackPane root = (StackPane) fullscreenStage.getScene().getRoot();
      ImageView view = (ImageView) root.getChildren().iterator().next();
      view.setImage(item.get().getImage());
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

  public GridPane getRoot() {
    return root;
  }
}
