package de.ks.imagecache;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

/**
 *
 */
public class Images {
  private static final Logger log = LogManager.getLogger(Images.class);
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
      log.error("Could not load image " + imagePath, e);
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
