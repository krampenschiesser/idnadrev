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

import de.ks.activity.ActivityController;
import de.ks.file.FileStore;
import de.ks.idnadrev.entity.Category;
import de.ks.imagecache.Images;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import javax.inject.Inject;
import java.io.File;

public class CategoryItemController {
  @Inject
  protected ActivityController controller;
  @Inject
  protected FileStore fileStore;

  @FXML
  protected Pane root;

  @FXML
  protected ImageView imageView;
  @FXML
  protected Label title;
  @FXML
  protected StackPane imageContainer;

  protected CategoryFilter filter;
  protected Category category;
  protected SimpleObjectProperty<Category> selectionProperty;

  public Pane getPane() {
    return root;
  }

  public void setFilter(CategoryFilter filter) {
    this.filter = filter;
  }

  public CategoryFilter getFilter() {
    return filter;
  }

  public void setCategory(Category category) {
    this.category = category;
    controller.getJavaFXExecutor().submit(this::applyContent);
  }

  public Category getCategory() {
    return category;
  }

  private void applyContent() {
    title.setText(category.getName());
    if (category.getImage() != null) {
      File file = fileStore.getFile(category.getImage());
      Image image = Images.get(file.getAbsolutePath());
      imageView.setImage(image);
    }
    root.setStyle("-fx-background-color: " + category.getColor() + "; -fx-background-radius: 15;");
    root.setOnMouseClicked(e -> {
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
}
