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
package de.ks.gallery.ui;

import de.ks.BaseController;
import de.ks.executor.group.LastExecutionGroup;
import de.ks.gallery.GalleryItem;
import de.ks.gallery.GalleryResource;
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

  private Collection<Thumbnail> thumbNails;
  private LastExecutionGroup<List<GalleryItem>> lastExecutionGroup;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

    lastExecutionGroup = new LastExecutionGroup<>("wait for file changes", 700, controller.getExecutorService());
    resource.getItems().addListener((ListChangeListener<GalleryItem>) c -> {
      CompletableFuture<List<GalleryItem>> cf = lastExecutionGroup.schedule(resource::getItems);
      cf.thenAcceptAsync(items -> recreate(items), controller.getJavaFXExecutor());
    });
  }

  public void setFiles(Collection<File> files) {
    resource.setFiles(files);
  }

  public void setFolder(File folder, boolean recurse) {
    resource.setFolder(folder, recurse);
  }

  protected void recreate(List<GalleryItem> galleryItems) {
    if (thumbNails != null) {
      cache.release(thumbNails);
    }
    log.info("Recreating gallery for {} items", galleryItems.size());
    List<GalleryItem> items = new ArrayList<>(galleryItems);
    CompletableFuture<Collection<Thumbnail>> reserve = cache.reserve(items.size());
    reserve.thenAcceptAsync(thumbnails -> {
      int i = 0;
      for (Thumbnail thumbnail : thumbnails) {
        GalleryItem item = items.get(i);
        thumbnail.setItem(item);
        i++;
      }

      container.getChildren().clear();
      ArrayList<Thumbnail> sorted = new ArrayList<>(thumbnails);
      Collections.sort(sorted, Comparator.comparing(t -> t.getItem()));
      sorted.forEach(item -> container.getChildren().add(item.getRoot()));
      this.thumbNails = thumbnails;
    }, controller.getJavaFXExecutor());
  }
}
