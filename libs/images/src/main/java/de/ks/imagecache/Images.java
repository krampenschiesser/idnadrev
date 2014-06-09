/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ks.imagecache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

/**
 *
 */
public class Images {
  private static final Logger log = LoggerFactory.getLogger(Images.class);
  private static final Images instance = new Images();

  private final LoadingCache<String, Image> cache;
  private final CacheLoader<? super String, Image> loader;

  private Images() {
    loader = new ImageLoader();
    cache = CacheBuilder.newBuilder()//
            .initialCapacity(300)//
            .softValues()//
            .build(loader);
  }

  public static Image get(String imagePath) {
    return instance.getImage(imagePath);
  }

  public static void later(String imagePath, AsyncImage applier) {
    instance.loadAsync(imagePath, applier);
  }

  protected Image getImage(String imagePath) {
    try {
      return cache.get(imagePath);
    } catch (ExecutionException | UncheckedExecutionException e) {
      log.error("Could not load image {}", imagePath, e);
      return null;
    }
  }

  public void loadAsync(String imagePath, AsyncImage applier) {
    ForkJoinPool.commonPool().submit(new Runnable() {
      @Override
      public void run() {
        Image image = getImage(imagePath);
        if (image != null) {
          applier.applyImage(image);
        }
      }
    });
  }
}
