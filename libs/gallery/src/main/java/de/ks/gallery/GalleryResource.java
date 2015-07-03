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
package de.ks.gallery;

import de.ks.activity.executor.ActivityExecutor;
import de.ks.executor.JavaFXExecutorService;
import de.ks.option.Options;
import javafx.collections.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class GalleryResource {
  private static final Logger log = LoggerFactory.getLogger(GalleryResource.class);

  protected final ObservableSet<File> files = FXCollections.observableSet(new LinkedHashSet<File>());
  protected final ObservableMap<File, GalleryItem> file2item = FXCollections.observableMap(new LinkedHashMap<File, GalleryItem>());
  protected final ObservableList<GalleryItem> items = FXCollections.observableArrayList();

  protected Supplier<GallerySettings> settingsSupplier = () -> Options.get(GallerySettings.class);
  protected final Set<File> parents = new HashSet<>();

  @Inject
  ActivityExecutor executor;
  @Inject
  JavaFXExecutorService javaFXExecutorService;

  private WatchService watchService;
  private Thread watchThread;

  protected GalleryResource() {
    clear();
    files.addListener((SetChangeListener<File>) c -> {
      int thumbNailSize = settingsSupplier.get().getThumbNailSize();

      HashSet<File> oldParents = new HashSet<File>(parents);
      HashSet<File> newParents = new HashSet<File>();

      for (File file : this.files) {
        newParents.add(file.getParentFile());

        if (!file2item.containsKey(file)) {
          createFile(file, thumbNailSize);
        }
      }
      if (watchService != null) {
        newParents.removeAll(oldParents);
        for (File file : newParents) {
          try {
            file.toPath().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
          } catch (IOException e) {
            log.error("Could not register {} at watchService.", file, e);
          }
        }
      }
    });
  }

  protected void createFile(File file, int thumbNailSize) {
    CompletableFuture<GalleryItem> future = CompletableFuture.supplyAsync(() -> {
      try {
        GalleryItem descriptor = new GalleryItem(file, thumbNailSize);
        return descriptor;
      } catch (Exception e) {
        log.info("Could not get image descriptor for {}", file, e);
        throw new RuntimeException(e);
      }
    }, executor);

    future.thenAcceptAsync(desc -> {
      File rf = desc.getFile();
      if (!file2item.containsKey(file)) {
        file2item.put(rf, desc);
        items.add(desc);
      }
    }, javaFXExecutorService);
  }

  public void setFiles(Collection<File> files) {
    javaFXExecutorService.submit(() -> {
      clear();
      this.files.addAll(files);
    });
  }

  public void clear() {
    this.files.clear();
    file2item.clear();
    items.clear();
    try {
      watchService.close();
    } catch (IOException e) {
      log.error("Could not close watchService", e);
    }
    watchService = null;
    try {
      watchService = FileSystems.getDefault().newWatchService();
      watchThread = new Thread(this::pollService);
      watchThread.setDaemon(true);
      watchThread.start();
    } catch (IOException e) {
      log.error("Could not open watchService", e);
    }
  }

  protected void pollService() {
    WatchKey key = watchService.poll();
    if (key.isValid()) {
      List<WatchEvent<?>> watchEvents = key.pollEvents();
      for (WatchEvent<?> watchEvent : watchEvents) {
        final WatchEvent<Path> wePath = (WatchEvent<Path>) watchEvent;
        Path path = wePath.context();
        File file = path.toFile();

        if (watchEvent.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
          handleItemDeleted(file);
        } else {
          handleItemModified(file);
        }
      }
    }
  }

  private void handleItemModified(File file) {
    int thumbNailSize = settingsSupplier.get().getThumbNailSize();
    handleItemDeleted(file);
    createFile(file, thumbNailSize);
  }

  private void handleItemDeleted(File file) {
    if (file2item.containsKey(file)) {
      javaFXExecutorService.submit(() -> {
        GalleryItem descriptor = file2item.remove(file);
        items.remove(descriptor);
      });
    }
  }

  public ObservableList<GalleryItem> getItems() {
    return FXCollections.unmodifiableObservableList(items);
  }
}
