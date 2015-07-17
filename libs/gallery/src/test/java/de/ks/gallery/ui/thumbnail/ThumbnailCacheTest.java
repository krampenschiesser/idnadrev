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

import de.ks.DummyActivityTest;
import de.ks.LauncherRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class ThumbnailCacheTest extends DummyActivityTest {

  @Inject
  ThumbnailCache cache;

  @Before
  public void setUp() throws Exception {
    cache.clear();

  }

  @Test
  public void testCache() throws Exception {
    Collection<Thumbnail> thumbnails = cache.reserve(50).get();

    assertEquals(50, thumbnails.size());
    assertEquals(0, cache.available.size());
    assertEquals(50, cache.reserved.size());

    Collection<Thumbnail> additional = cache.reserve(10).get();
    assertEquals(0, cache.available.size());
    assertEquals(60, cache.reserved.size());

    cache.release(thumbnails);
    assertEquals(50, cache.available.size());
    assertEquals(10, cache.reserved.size());

    thumbnails = cache.reserve(49).get();
    Collection<Thumbnail> single = cache.reserve(1).get();
    assertEquals(0, cache.available.size());
    assertEquals(60, cache.reserved.size());

    cache.release(single);
    cache.reserve(1).get();
    assertEquals(0, cache.available.size());
    assertEquals(60, cache.reserved.size());
  }

  @Test
  public void testMutliThreadAccess() throws Exception {
    int nThreads = 8;
    int amountPerThread = 10;
//    ExecutorService executorService = Executors.newFixedThreadPool(1);
    ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

    ArrayList<Future<CompletableFuture<List<Thumbnail>>>> futures = new ArrayList<>();
    for (int i = 0; i < nThreads; i++) {
      futures.add(executorService.submit(() -> {
        return cache.reserve(amountPerThread);
      }));
    }

    for (Future<CompletableFuture<List<Thumbnail>>> future : futures) {
      Collection<Thumbnail> thumbnails = future.get().get();
      assertEquals(amountPerThread, thumbnails.size());
    }

    assertEquals(0, cache.available.size());
    assertEquals(nThreads * amountPerThread, cache.reserved.size());
  }
}
