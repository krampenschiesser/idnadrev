/**
 * Copyright [2015] [Christian Loehnert]
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.gallery.ui.thumbnail;

import de.ks.gallery.GalleryItem;
import de.ks.gallery.GalleryResource;
import de.ks.gallery.ui.slideshow.Slideshow;
import de.ks.standbein.BaseController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class ThumbnailGallery extends BaseController<Object> {
  private static final Logger log = LoggerFactory.getLogger(ThumbnailGallery.class);
  @Inject
  protected GalleryResource resource;
  @Inject
  protected ThumbnailCache cache;

  @FXML
  protected StackPane root;
  @FXML
  protected ProgressIndicator loader;
  @FXML
  protected StackPane container;

  protected final Set<Thumbnail> allThumbNails = new HashSet<>();
  protected Slideshow slideshow;

  protected BiFunction<Control, Thumbnail, Node> enhancer;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    slideshow = activityInitialization.loadAdditionalController(Slideshow.class).getController();
    resource.setCallback(items -> {
      addItems(items);
      slideshow.getItems().addAll(items);
    });
    root.widthProperty().addListener((p, o, n) -> {
      int itemsInRoot = Math.max(0, (int) ((Double) n / Thumbnail.DEFAULT_WIDTH));
      relayoutThumbNails(itemsInRoot);
    });
    showLoader();
  }

  public void setFiles(Collection<File> files) {
    controller.getJavaFXExecutor().submit(this::showLoader);
    resource.setFiles(files);
  }

  public void showLoader() {
    loader.setProgress(-1);
    loader.setVisible(true);
    container.setVisible(false);
  }

  public void hideLoader() {
    loader.setVisible(false);
    container.setVisible(true);
  }

  public void setFolder(File folder, boolean recurse) {
    controller.getJavaFXExecutor().submit(this::showLoader);
    resource.setFolder(folder, recurse);
  }

  protected void addItems(List<GalleryItem> galleryItems) {
    log.info("Recreating gallery for {} items", galleryItems.size());

    releaseThumbnails();
    container.getChildren().clear();
    allThumbNails.clear();

    List<GalleryItem> items = new ArrayList<>(galleryItems);
    CompletableFuture<List<Thumbnail>> reserve = cache.reserve(items.size());

    reserve.thenApplyAsync(thumbnails -> {
      int i = 0;
      hideLoader();
      for (Thumbnail thumbnail : thumbnails) {
        GalleryItem item = items.get(i);
        thumbnail.setSlideshow(slideshow);
        thumbnail.setItem(item);
        i++;

        allThumbNails.add(thumbnail);
      }

      int itemsInRoot = Math.max(0, (int) root.getWidth() / Thumbnail.DEFAULT_WIDTH);
      relayoutThumbNails(itemsInRoot);
      return thumbnails;
    }, controller.getJavaFXExecutor());
  }

  protected void relayoutThumbNails(int itemsInRoot) {
    container.getChildren().clear();

    ObservableList<GridPane> items = FXCollections.observableArrayList();
    Map<GridPane, List<Thumbnail>> box2Thumbnail = new HashMap<>();

    GridPane gridPane = createPane(itemsInRoot, items);
    box2Thumbnail.put(gridPane, new ArrayList<>(5));
    int i = 1;

    ArrayList<Thumbnail> thumbnails = new ArrayList<>(allThumbNails);
    Collections.sort(thumbnails, Comparator.comparing(t -> t.getItem()));
    for (Thumbnail thumbnail : thumbnails) {
      log.info("Processing thumbnail {}", thumbnail.getItem().getName());
      Control root = thumbnail.getRoot();
      box2Thumbnail.get(gridPane).add(thumbnail);
      Node enhance = enhance(root, thumbnail);
      gridPane.add(enhance, i - 1, 0);
      i++;
      if (i > itemsInRoot) {
        i = 1;
        gridPane = createPane(itemsInRoot, items);
        box2Thumbnail.put(gridPane, new ArrayList<>(5));
      }
    }

    ListView<GridPane> listView = new ListView<>();
    listView.setCellFactory(new Callback<ListView<GridPane>, ListCell<GridPane>>() {
      @Override
      public ListCell<GridPane> call(ListView<GridPane> param) {
        ListCell<GridPane> cell = new ListCell<GridPane>() {
          @Override
          protected void updateItem(GridPane item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
              box2Thumbnail.get(item).forEach(t -> {
                CompletableFuture.supplyAsync(() -> t.getItem().getThumbNail(), controller.getExecutorService())//
                  .thenAcceptAsync(image -> {
                    ImageView imageView = t.getImageView();
                    if (image != null) {
                      imageView.setImage(image);
                      imageView.setFitHeight(image.getHeight());
                      imageView.setFitWidth(image.getWidth());
                    } else {
                      imageView.setImage(null);
                    }
                  }, controller.getJavaFXExecutor());
              });
              setGraphic(item);
              log.info("Updating item {}", items.indexOf(item));
            } else {
              setGraphic(null);
            }
          }
        };
        return cell;
      }
    });
    listView.setItems(items);
    container.getChildren().add(listView);
  }

  private GridPane createPane(int itemsInRoot, ObservableList<GridPane> items) {
    GridPane gridPane = new GridPane();
    for (int i = 0; i < itemsInRoot; i++) {
      gridPane.getColumnConstraints().add(new ColumnConstraints(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE, Priority.SOMETIMES, HPos.CENTER, true));
    }
    items.add(gridPane);
    return gridPane;
  }

  private Node enhance(Control root, Thumbnail thumbnail) {
    if (enhancer != null) {
      return enhancer.apply(root, thumbnail);
    }
    return root;
  }

  private void releaseThumbnails() {
    cache.release(allThumbNails);
  }

  public Pane getRoot() {
    return root;
  }

  public Set<Thumbnail> getAllThumbNails() {
    return allThumbNails;
  }

  public BiFunction<Control, Thumbnail, Node> getEnhancer() {
    return enhancer;
  }

  public void setEnhancer(BiFunction<Control, Thumbnail, Node> enhancer) {
    this.enhancer = enhancer;
  }

  public ProgressIndicator getLoader() {
    return loader;
  }

  public Slideshow getSlideshow() {
    return slideshow;
  }
}
