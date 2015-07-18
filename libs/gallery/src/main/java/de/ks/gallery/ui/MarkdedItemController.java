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

import de.ks.BaseController;
import de.ks.gallery.GalleryItem;
import de.ks.gallery.ui.slideshow.Slideshow;
import de.ks.reflection.PropertyPath;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.ResourceBundle;

public class MarkdedItemController extends BaseController<Object> {
  private static final Logger log = LoggerFactory.getLogger(MarkdedItemController.class);
  @FXML
  protected TabPane root;
  @FXML
  protected TableView<GalleryItem> markedTable;
  @FXML
  protected TableView<GalleryItem> markedForDeletionTable;

  @FXML
  private TableColumn<GalleryItem, String> markedName;
  @FXML
  private TableColumn<GalleryItem, String> markedRemove;
  @FXML
  private TableColumn<GalleryItem, String> deletionName;
  @FXML
  private TableColumn<GalleryItem, String> deletionRemove;

  @FXML
  protected HBox markedActionContainer;
  @FXML
  protected Button deleteMarkedForDeletion;

  protected final ObservableList<GalleryItem> marked = FXCollections.observableArrayList();
  protected final ObservableList<GalleryItem> markedForDeletion = FXCollections.observableArrayList();
  private Slideshow slideshow;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    markedTable.setItems(marked);
    markedForDeletionTable.setItems(markedForDeletion);

    String property = PropertyPath.property(GalleryItem.class, g -> g.getName());

    markedName.setCellValueFactory(new PropertyValueFactory<>(property));
    markedRemove.setCellValueFactory(new PropertyValueFactory<>(property));
    deletionName.setCellValueFactory(new PropertyValueFactory<>(property));
    deletionRemove.setCellValueFactory(new PropertyValueFactory<>(property));

    markedRemove.setCellFactory(createRemoveCallback(marked));
    deletionRemove.setCellFactory(createRemoveCallback(markedForDeletion));
    markedName.setCellFactory(createLinkCallback(marked));
    deletionName.setCellFactory(createLinkCallback(markedForDeletion));

    deleteMarkedForDeletion.disableProperty().bind(store.loadingProperty());
  }

  protected Callback<TableColumn<GalleryItem, String>, TableCell<GalleryItem, String>> createLinkCallback(ObservableList<GalleryItem> collection) {
    return column -> new TableCell<GalleryItem, String>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null) {
          setGraphic(null);
        } else {
          Hyperlink link = new Hyperlink(item);
          link.setOnAction(e -> {
            GalleryItem galleryItem = collection.stream().filter(i -> i.getName().equals(item)).findFirst().get();
            if (slideshow != null) {
              slideshow.show(galleryItem);
            }
          });
          setGraphic(link);
        }
      }
    };
  }

  protected Callback<TableColumn<GalleryItem, String>, TableCell<GalleryItem, String>> createRemoveCallback(ObservableList<GalleryItem> collection) {
    return column -> new TableCell<GalleryItem, String>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null) {
          setGraphic(null);
        } else {
          Button button = new Button("(X)");
          button.setOnAction(e -> collection.remove(collection.stream().filter(i -> i.getName().equals(item)).findFirst().get()));
          setGraphic(button);
        }
      }
    };
  }

  @FXML
  protected void onDeleteMarkedForDeletion() {
    store.executeCustomRunnable(() -> {
      HashSet<GalleryItem> items = new HashSet<>(markedForDeletion);
      for (GalleryItem item : items) {
        File file = item.getFile();
        if (file.exists()) {

          try {
            Files.delete(file.toPath());
          } catch (IOException e) {
            log.error("Could not delete file {}", file, e);
          }
        }
      }
      controller.getJavaFXExecutor().execute(() -> markedForDeletion.clear());
    });
  }

  public TabPane getRoot() {
    return root;
  }

  public ObservableList<GalleryItem> getMarkedForDeletion() {
    return markedForDeletion;
  }

  public ObservableList<GalleryItem> getMarked() {
    return marked;
  }

  public void bindTo(Slideshow slideshow) {
    Bindings.bindContentBidirectional(marked, slideshow.getMarkedItems());
    Bindings.bindContentBidirectional(markedForDeletion, slideshow.getMarkedForDeletion());
    this.slideshow = slideshow;
  }
}
