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
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class GalleryResource {
  private static final Logger log = LoggerFactory.getLogger(GalleryResource.class);

  protected final ObservableSet<File> files = FXCollections.observableSet(new LinkedHashSet<File>());
  protected final ObservableMap<File, GalleryItem> file2item = FXCollections.observableMap(new LinkedHashMap<File, GalleryItem>());
  protected final ObservableList<GalleryItem> items = FXCollections.observableArrayList();

  protected Supplier<GallerySettings> settingsSupplier = () -> Options.get(GallerySettings.class);
  protected final Set<File> parents = new HashSet<>();
  protected final Map<WatchKey, File> key2Dir = new ConcurrentHashMap<>();
  protected final Set<File> knownDeleted = Collections.synchronizedSet(new HashSet<>());

  @Inject
  ActivityExecutor executor;
  @Inject
  JavaFXExecutorService javaFXExecutorService;

  private WatchService watchService;
  private Thread watchThread;

  protected GalleryResource() {
    reset();
    files.addListener((SetChangeListener<File>) c -> {
      int thumbNailSize = getThumbnailSize();

      File file = c.getElementAdded();
      File parent = file.getParentFile();

      if (!file2item.containsKey(file)) {
        createFile(file, thumbNailSize);
      }
      if (watchService != null && !parents.contains(parent)) {
        try {
          WatchKey watchKey = parent.toPath().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_CREATE);
          parents.add(parent);
          key2Dir.put(watchKey, parent);
          log.debug("Registered {} at watchservice.", parent);
        } catch (IOException e) {
          log.error("Could not register {} at watchService.", parent, e);
        }
      }
    });
  }

  protected void createFile(File file, int thumbNailSize) {
    CompletableFuture<GalleryItem> future = CompletableFuture.supplyAsync(() -> {
      try {
        GalleryItem descriptor = new GalleryItem(file, thumbNailSize);
        log.debug("Created gallery item for {}", file);
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

  public void setFolder(File folder, boolean recurse) {
    if (!folder.isDirectory()) {
      throw new IllegalArgumentException("Given file " + folder + " is no folder");
    }
    ArrayList<File> files = new ArrayList<>();
    SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        files.add(file.toFile());
        return super.visitFile(file, attrs);
      }

      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (dir.toFile().equals(folder)) {
          return super.preVisitDirectory(dir, attrs);
        } else if (recurse) {
          return FileVisitResult.CONTINUE;
        } else {
          return FileVisitResult.SKIP_SUBTREE;
        }
      }
    };
    try {
      Files.walkFileTree(folder.toPath(), visitor);
    } catch (IOException e) {
      log.error("Could not walk filetree {}", folder);
    }
    setFiles(files);
  }

  public void setFiles(Collection<File> files) {
    javaFXExecutorService.submit(() -> {
      reset();
      this.files.addAll(files);
    });
  }

  public void reset() {
    this.files.clear();
    file2item.clear();
    items.clear();
    key2Dir.clear();
    knownDeleted.clear();
    if (watchService != null) {
      try {
        watchService.close();
      } catch (IOException e) {
        log.error("Could not close watchService", e);
      }
    }
    watchService = null;
    try {
      watchService = FileSystems.getDefault().newWatchService();
      watchThread = new Thread(this::pollService);
      watchThread.setName("WatchService-Poll-" + getClass().getSimpleName());
      watchThread.setDaemon(true);
      watchThread.start();
    } catch (IOException e) {
      log.error("Could not open watchService", e);
    }
  }

  protected void pollService() {
    try {
      while (true) {
        WatchKey key = watchService.poll();
        if (key != null && key.isValid()) {
          File parentDir = key2Dir.get(key);

          List<WatchEvent<?>> watchEvents = key.pollEvents();
          for (WatchEvent<?> watchEvent : watchEvents) {
            final WatchEvent<Path> wePath = (WatchEvent<Path>) watchEvent;
            Path path = wePath.context();
            File file = new File(parentDir, path.toFile().getName());
            log.trace("Got watchevent {} with file {} and kind {}", watchEvent, file.getAbsolutePath(), watchEvent.kind());

            if (watchEvent.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
              handleItemDeleted(file);
              knownDeleted.add(file);
            } else if (watchEvent.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
              handleItemModified(file);
            } else if (watchEvent.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
              handleItemCreated(file);
            }
          }
          key.reset();
        }
      }
    } catch (ClosedWatchServiceException e) {
      log.debug("Closed watchservice normally");
    } catch (Exception e) {
      log.error("Exception while polling on watchservice ", e);
    }
  }

  private void handleItemCreated(File file) {
    if (knownDeleted.contains(file)) {
      log.debug("Got previously deleted file back again {}", file);
      int thumbNailSize = getThumbnailSize();
      createFile(file, thumbNailSize);
    }
  }

  private void handleItemModified(File file) {
    int thumbNailSize = getThumbnailSize();
    handleItemDeleted(file);
    createFile(file, thumbNailSize);
  }

  public int getThumbnailSize() {
    return settingsSupplier.get().getThumbNailSize();
  }

  private void handleItemDeleted(File file) {
    if (file2item.containsKey(file)) {
      log.debug("File {} was deleted, removing it.", file);
      javaFXExecutorService.submit(() -> {
        GalleryItem descriptor = file2item.remove(file);
        items.remove(descriptor);
        log.debug("Successfully removed {}.", file);
      });
    }
  }

  public ObservableList<GalleryItem> getItems() {
    return items;
  }
}
