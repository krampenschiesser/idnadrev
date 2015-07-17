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
package de.ks.gallery.ui.thumbnail;

import de.ks.activity.executor.ActivityExecutor;
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
  protected ActivityExecutor javaFXExecutor;

  protected Set<Thumbnail> available = new HashSet<>();
  protected Set<Thumbnail> reserved = new HashSet<>();

  protected CompletableFuture<List<Thumbnail>> load(int amount) {
    CompletableFuture<List<Thumbnail>> result = CompletableFuture.completedFuture(Collections.synchronizedList(new ArrayList<>(amount)));
    for (int i = 0; i < amount; i++) {
      CompletableFuture<Thumbnail> future = CompletableFuture.supplyAsync(this::load, executor).thenApply(t -> {
        synchronized (this) {
          reserved.add(t);
        }
        return t;
      });

      result = future.thenCombine(result, (thumbnail, thumbnails) -> {
        thumbnails.add(thumbnail);
        return thumbnails;
      });
    }
    return result;
  }

  public synchronized CompletableFuture<List<Thumbnail>> reserve(int amount) {
//    return load(amount);
    int toLoad = amount - available.size();
    int usedAvailable = amount - Math.max(0, toLoad);

    log.debug("For requested amount {}, need to load {} using available {}/{}", amount, toLoad, usedAvailable, available.size());
    List<Thumbnail> initiallyReserved;
    if (usedAvailable > 0) {
      initiallyReserved = getAvailablePushToReserved(usedAvailable);
    } else {
      initiallyReserved = Collections.emptyList();
    }
    Function<List<Thumbnail>, List<Thumbnail>> combiner = c -> {
      HashSet<Thumbnail> all = new HashSet<>(c);
      all.addAll(initiallyReserved);
      return new ArrayList<>(all);
    };
    if (toLoad > 0) {
      CompletableFuture<List<Thumbnail>> cf = load(toLoad);
      return cf.thenApply(combiner);
    } else {
      return CompletableFuture.completedFuture(initiallyReserved);
    }
  }

  protected List<Thumbnail> getAvailablePushToReserved(int amount) {
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
    return new ArrayList<>(retval);
  }

  public synchronized void release(Collection<Thumbnail> thumbnails) {
    thumbnails.forEach(t -> t.reset());
    reserved.removeAll(thumbnails);
    available.addAll(thumbnails);
  }

  protected Thumbnail load() {
    return new Thumbnail();
  }

  public void clear() {
    available.clear();
    reserved.clear();
  }
}
