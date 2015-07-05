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

import de.ks.BaseController;
import de.ks.executor.group.LastExecutionGroup;
import de.ks.gallery.GalleryItem;
import de.ks.gallery.GalleryResource;
import de.ks.gallery.ui.slideshow.Slideshow;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
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
  protected FlowPane container;

  private LastExecutionGroup<List<GalleryItem>> lastExecutionGroup;
  protected final Set<Thumbnail> allThumbNails = new HashSet<>();
  private Slideshow slideShow;

  protected BiFunction<Node, Thumbnail, Node> enhancer;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    slideShow = activityInitialization.loadAdditionalController(Slideshow.class).getController();
    Bindings.bindContent(slideShow.getItems(), resource.getItems());

    lastExecutionGroup = new LastExecutionGroup<>("wait for file changes", 700, controller.getExecutorService());
    resource.getItems().addListener((ListChangeListener<GalleryItem>) c -> {
      while (c.next()) {
        List<GalleryItem> added = (List<GalleryItem>) c.getAddedSubList();

        addItems(added, false, false);
      }

      CompletableFuture<List<GalleryItem>> cf = lastExecutionGroup.schedule(resource::getItems);
      if (cf.getNumberOfDependents() == 0) {
        cf.thenAcceptAsync(items -> addItems(items, true, true), controller.getJavaFXExecutor());
      }
    });
  }

  public void setFiles(Collection<File> files) {
    controller.getJavaFXExecutor().submit(this::showLoader);
    resource.setFiles(files);
  }

  protected void showLoader() {
    loader.setProgress(-1);
    loader.setVisible(true);
  }

  protected void hideLoader() {
    loader.setVisible(false);
  }

  public void setFolder(File folder, boolean recurse) {
    controller.getJavaFXExecutor().submit(this::showLoader);
    resource.setFolder(folder, recurse);
  }

  protected void addItems(List<GalleryItem> galleryItems, boolean clear, boolean sort) {
    log.info("Recreating gallery for {} items", galleryItems.size());

    if (clear) {
      releaseThumbnails();
      container.getChildren().clear();
      allThumbNails.clear();
    }

    List<GalleryItem> items = new ArrayList<>(galleryItems);
    CompletableFuture<Collection<Thumbnail>> reserve = cache.reserve(items.size());

    reserve.thenAcceptAsync(thumbnails -> {
      int i = 0;
      hideLoader();
      for (Thumbnail thumbnail : thumbnails) {
        GalleryItem item = items.get(i);
        thumbnail.setSlideshow(slideShow);
        thumbnail.setItem(item);
        i++;
        Node root = thumbnail.getRoot();
        root = enhance(root, thumbnail);
        container.getChildren().add(root);
        allThumbNails.add(thumbnail);
      }

      if (sort) {
        ArrayList<Thumbnail> sorted = new ArrayList<>(allThumbNails);
        Collections.sort(sorted, Comparator.comparing(t -> t.getItem()));
        container.getChildren().clear();
        sorted.forEach(item -> {
          Node root = item.getRoot();
          root = enhance(root, item);
          container.getChildren().add(root);
        });
      }
    }, controller.getJavaFXExecutor());
  }

  private Node enhance(Node root, Thumbnail thumbnail) {
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

  public BiFunction<Node, Thumbnail, Node> getEnhancer() {
    return enhancer;
  }

  public void setEnhancer(BiFunction<Node, Thumbnail, Node> enhancer) {
    this.enhancer = enhancer;
  }
}
