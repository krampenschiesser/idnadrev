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
import javafx.scene.layout.FlowPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ThumbnailGallery extends BaseController<Object> {
  private static final Logger log = LoggerFactory.getLogger(ThumbnailGallery.class);
  @Inject
  protected GalleryResource resource;
  @Inject
  protected ThumbnailCache cache;

  @FXML
  protected FlowPane container;

  private LastExecutionGroup<List<GalleryItem>> lastExecutionGroup;
  protected final Set<Thumbnail> allThumbNails = new HashSet<>();
  @Inject
  private Slideshow slideShow;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
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
    resource.setFiles(files);
  }

  public void setFolder(File folder, boolean recurse) {
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
      for (Thumbnail thumbnail : thumbnails) {
        GalleryItem item = items.get(i);
        thumbnail.setSlideshow(slideShow);
        thumbnail.setItem(item);
        i++;
        container.getChildren().add(thumbnail.getRoot());
        allThumbNails.add(thumbnail);
      }

      if (sort) {
        ArrayList<Thumbnail> sorted = new ArrayList<>(allThumbNails);
        Collections.sort(sorted, Comparator.comparing(t -> t.getItem()));
        container.getChildren().clear();
        sorted.forEach(item -> container.getChildren().add(item.getRoot()));
      }
    }, controller.getJavaFXExecutor());
  }

  private void releaseThumbnails() {
    cache.release(allThumbNails);
  }

  public FlowPane getRoot() {
    return container;
  }

  public Set<Thumbnail> getAllThumbNails() {
    return allThumbNails;
  }
}
