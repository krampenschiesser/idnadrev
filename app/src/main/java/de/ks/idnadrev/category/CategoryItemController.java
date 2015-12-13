/**
 * Copyright [2014] [Christian Loehnert]
 *
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
package de.ks.idnadrev.category;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import javax.inject.Inject;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class CategoryItemController implements Initializable {
  @Inject
  protected ActivityController controller;
  @Inject
  protected FileStore fileStore;

  @FXML
  protected Pane root;
  @FXML
  protected StackPane container;
  @FXML
  protected ImageView imageView;
  @FXML
  protected Button title;

  protected Category category;
  protected SimpleObjectProperty<Category> selectionProperty;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    title.getStyleClass().add("categoryBrowseItem");
    container.getStyleClass().add("categoryBrowseItemContainer");
  }

  private void applyContent() {
    title.setText(category.getName());
    if (category.getImage() != null) {
      File file = fileStore.getFile(category.getImage());
      Image image = Images.get(file.getAbsolutePath());
      imageView.setImage(image);
    }

    container.setStyle("-fx-background-color: -fx-outer-border, -fx-inner-border, linear-gradient(to bottom, derive(" + category.getColorAsString() + ",20%) ,derive(" + category.getColorAsString() + ",-13%));");// + category.getColor() + ";");

    title.setOnAction(e -> {
      if (selectionProperty != null) {
        selectionProperty.set(category);
      }
    });
  }

  public void setSelectionProperty(SimpleObjectProperty<Category> selectionProperty) {
    this.selectionProperty = selectionProperty;
  }

  public SimpleObjectProperty<Category> getSelectionProperty() {
    return selectionProperty;
  }

  public Pane getPane() {
    return root;
  }

  public void setCategory(Category category) {
    this.category = category;
    controller.getJavaFXExecutor().submit(this::applyContent);
  }

  public Category getCategory() {
    return category;
  }
}
