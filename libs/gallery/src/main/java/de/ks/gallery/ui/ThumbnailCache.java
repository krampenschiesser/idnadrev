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

import de.ks.activity.executor.ActivityExecutor;
import de.ks.application.fxml.DefaultLoader;
import de.ks.executor.JavaFXExecutorService;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Singleton
public class ThumbnailCache {
  private static final Logger log = LoggerFactory.getLogger(ThumbnailCache.class);
  @Inject
  protected ActivityExecutor executor;
  @Inject
  protected JavaFXExecutorService javaFXExecutorService;

  protected Set<Thumbnail> available = new HashSet<>();
  protected Set<Thumbnail> reserved = new HashSet<>();

  protected CompletableFuture<ArrayList<Thumbnail>> load(int amount) {
    CompletableFuture<ArrayList<Thumbnail>> result = CompletableFuture.completedFuture(new ArrayList<Thumbnail>(amount));
    for (int i = 0; i < amount; i++) {
      CompletableFuture<Thumbnail> future = CompletableFuture.supplyAsync(this::load, executor).thenApplyAsync(t -> {
        reserved.add(t);
        return t;
      }, javaFXExecutorService);

      result = future.thenCombineAsync(result, (thumbnail, thumbnails) -> {
        thumbnails.add(thumbnail);
        return thumbnails;
      }, javaFXExecutorService);
    }
    return result;
  }

  public synchronized CompletableFuture<Collection<Thumbnail>> reserve(int amount) {
    int toLoad = amount - available.size();
    int usedAvailable = amount - Math.max(0, toLoad);

    log.debug("For requested amount {}, need to load {} using available {}/{}", amount, toLoad, usedAvailable, available.size());
    Collection<Thumbnail> initiallyReserved;
    if (usedAvailable > 0) {
      initiallyReserved = getAvailablePushToReserved(usedAvailable);
    } else {
      initiallyReserved = Collections.emptyList();
    }
    Function<Collection<Thumbnail>, Collection<Thumbnail>> combiner = c -> {
      HashSet<Thumbnail> all = new HashSet<>(c);
      all.addAll(initiallyReserved);
      return all;
    };
    if (toLoad > 0) {
      CompletableFuture<ArrayList<Thumbnail>> cf = load(toLoad);
      return cf.thenApply(combiner);
    } else {
      return CompletableFuture.completedFuture(initiallyReserved);
    }
  }

  protected Collection<Thumbnail> getAvailablePushToReserved(int amount) {
    HashSet<Thumbnail> retval = new HashSet<>();
    Iterator<Thumbnail> iterator = available.iterator();
    for (int i = 0; i < amount; i++) {
      if (!iterator.hasNext()) {
        throw new IllegalStateException("Expected to have at least " + (amount - i) + " more elements but there are only " + available.size());
      }
      Thumbnail next = iterator.next();
      iterator.remove();
      retval.add(next);
      reserved.add(next);
      log.debug("Used {} from {} available to {} reserved.", amount, available.size(), reserved.size());
    }
    return retval;
  }

  public synchronized void release(Collection<Thumbnail> thumbnails) {
    reserved.removeAll(thumbnails);
    available.addAll(thumbnails);
  }

  protected Thumbnail load() {
    DefaultLoader<GridPane, Thumbnail> load = new DefaultLoader<GridPane, Thumbnail>(Thumbnail.class).load();
    return load.getController();
  }

  public void clear() {
    available.clear();
    reserved.clear();
  }
}
