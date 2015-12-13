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

import com.google.common.net.MediaType;
import de.ks.executor.JavaFXExecutorService;
import de.ks.standbein.activity.executor.ActivityExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class GalleryResource {
  private static final Logger log = LoggerFactory.getLogger(GalleryResource.class);

  private final Set<File> files = new LinkedHashSet<>();
  private final ConcurrentHashMap<File, GalleryItem> items = new ConcurrentHashMap<>();
  private Consumer<List<GalleryItem>> callback;

  protected final Set<File> parents = new HashSet<>();
  protected final Map<WatchKey, File> key2Dir = new ConcurrentHashMap<>();
  protected final Set<File> knownDeleted = Collections.synchronizedSet(new HashSet<>());

  @Inject
  ActivityExecutor executor;
  @Inject
  JavaFXExecutorService javaFXExecutorService;
  @Inject
  Provider<GallerySettings> settingsProvider;

  private WatchService watchService;
  private Thread watchThread;

  private final AtomicReference<String> currentLoad = new AtomicReference<>();

  protected GalleryResource() {
    reset();
  }

  protected GalleryItem createItem(File file, int thumbNailSize) {
    try {
      String contentType = Files.probeContentType(file.toPath());
      if (contentType == null) {
        return null;
      }
      MediaType parse = MediaType.parse(contentType);
      if (!parse.is(MediaType.ANY_IMAGE_TYPE)) {
        log.debug("File {} is no image", file);
        return null;
      }
    } catch (IOException e) {
      log.error("Could not probe content type of {}", file, e);
      return null;
    }

    try {
      GalleryItem descriptor = new GalleryItem(file, thumbNailSize, executor);
      log.debug("Created gallery item for {}", file);
      return descriptor;
    } catch (Exception e) {
      log.info("Could not get image descriptor for {}", file, e);
      throw new RuntimeException(e);
    }
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

  public synchronized void setFiles(Collection<File> files) {
    ArrayList<File> sorted = new ArrayList<>(files);
    sorted.removeAll(this.files);

    Collections.sort(sorted);
    this.files.retainAll(files);
    this.files.addAll(files);
    this.items.keySet().retainAll(files);

    final String currentLoadIdentifier = UUID.randomUUID().toString();
    currentLoad.set(currentLoadIdentifier);

    int thumbNailSize = getThumbnailSize();
    CompletableFuture<Void> combined = null;
    for (File file : sorted) {
      parents.add(file.getParentFile());
      CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
        if (currentLoad.get().equals(currentLoadIdentifier)) {
          return createItem(file, thumbNailSize);
        } else {
          return null;
        }
      }, executor).thenAccept(item -> {
        if (item != null) {
          items.put(item.getFile(), item);
        }
      });
      if (combined == null) {
        combined = future;
      } else {
        combined = CompletableFuture.allOf(combined, future);
      }
    }
    if (combined != null) {
      combined.thenApply(bla -> getItemsSorted(currentLoadIdentifier)).thenApplyAsync((List<GalleryItem> all) -> {
        if (callback != null) {
          callback.accept(all);
        }
        return all;
      }, javaFXExecutorService).exceptionally(t -> {
        log.error("Could not add items", t);
        return null;
      });
    }

    recreateWatchService();
    parents.forEach(p -> {
      try {
        WatchKey register = p.toPath().register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        key2Dir.put(register, p);
      } catch (IOException e) {
        log.error("Could not register {} at watchservice", p, e);
      }
    });
  }

  protected List<GalleryItem> getItemsSorted(String currentLoadIdentifier) {
    List<GalleryItem> values = new ArrayList<>(items.values());
    if (currentLoad.get().equals(currentLoadIdentifier)) {
      Collections.sort(values);
      log.info("Got all");
      return values;
    } else {
      return Collections.<GalleryItem>emptyList();
    }
  }

  public synchronized void reset() {
    this.files.clear();
    key2Dir.clear();
    knownDeleted.clear();
    recreateWatchService();
  }

  protected void recreateWatchService() {
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
      GalleryItem item = createItem(file, thumbNailSize);
      submitItem(item);
    }
  }

  private void submitItem(GalleryItem item) {
    items.put(item.getFile(), item);
    List<GalleryItem> values = new ArrayList<>(items.values());
    Collections.sort(values);
    javaFXExecutorService.submit(() -> {
      if (callback != null) {
        callback.accept(values);
      }
    });
  }

  private void handleItemModified(File file) {
    int thumbNailSize = getThumbnailSize();
    handleItemDeleted(file);
    GalleryItem item = createItem(file, thumbNailSize);
    submitItem(item);
  }

  public int getThumbnailSize() {
    return settingsProvider.get().getThumbNailSize();
  }

  private void handleItemDeleted(File file) {
    if (items.containsKey(file)) {
      knownDeleted.add(file);
      log.debug("File {} was deleted, removing it.", file);
      items.remove(file);
      List<GalleryItem> itemsSorted = getItemsSorted(currentLoad.get());
      javaFXExecutorService.submit(() -> {
        if (callback != null) {
          callback.accept(itemsSorted);
        }
      });
    }
  }

  public void setCallback(Consumer<List<GalleryItem>> callback) {
    this.callback = callback;
  }

  public Consumer<List<GalleryItem>> getCallback() {
    return callback;
  }

  public Collection<GalleryItem> getItems() {
    return items.values();
  }
}
