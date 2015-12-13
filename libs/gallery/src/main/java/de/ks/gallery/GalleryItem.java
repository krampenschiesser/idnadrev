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

import de.ks.standbein.activity.executor.ActivityExecutor;
import de.ks.standbein.javafx.ScreenResolver;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.stage.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.time.LocalDateTime;
import java.util.Optional;

public class GalleryItem implements Comparable<GalleryItem> {
  private static final Logger log = LoggerFactory.getLogger(GalleryItem.class);
  protected final File file;
  protected final String name;
  private final int thumbnailSize;

  protected volatile Image thumbNail;

  protected final Optional<LocalDateTime> shootingTime;

  protected SoftReference<Image> loadedImage;

  public GalleryItem(File file, int thumbnailSize, ActivityExecutor executor) throws IOException {
    this.thumbnailSize = thumbnailSize;
    if (file == null) {
      throw new IllegalArgumentException("Cannot create from null file ");
    }
    if (!file.exists()) {
      throw new IllegalArgumentException("Cannot create from non-existing file " + file);
    }

    this.file = file;
    name = file.getName();

    shootingTime = new ImageScaler().getShootingTime(file);
  }

  public Image getThumbNail() {
    if (thumbNail == null) {
      synchronized (this) {
        if (thumbNail == null) {
          try {
            ImageScaler scaler = new ImageScaler();
            log.debug("Loading thumbnail for {}", file);
            BufferedImage thumbNailImage = scaler.rotateAndScale(file, thumbnailSize);
            thumbNail = SwingFXUtils.toFXImage(thumbNailImage, new WritableImage(thumbNailImage.getWidth(), thumbNailImage.getHeight()));
          } catch (IOException e) {
            return new WritableImage(thumbnailSize, thumbnailSize);
          }
        }
      }
    }
    return thumbNail;
  }

  public File getFile() {
    return file;
  }

  public String getName() {
    return name;
  }

  public Image getImage() {
    if (loadedImage != null) {
      Image image = loadedImage.get();
      if (image != null) {
        return image;
      }
    }
    Image image = loadImage();
    loadedImage = new SoftReference<>(image);
    return image;
  }

  protected Image loadImage() {
    Screen screen = new ScreenResolver().getScreenToShow();
    double height = screen.getBounds().getHeight();
    double width = screen.getBounds().getWidth();

    try {
      ImageScaler scaler = new ImageScaler();
      BufferedImage originalRotated = scaler.rotateAndScale(file, (int) width, (int) height);
      return SwingFXUtils.toFXImage(originalRotated, new WritableImage(originalRotated.getWidth(), originalRotated.getHeight()));
    } catch (IOException e) {
      throw new RuntimeException(e);//should not happen has constructor should have failed
    }
  }

  public Optional<LocalDateTime> getShootingTime() {
    return shootingTime;
  }

  public void clear() {
    if (loadedImage != null) {
      loadedImage = null;
    }
  }

  public boolean isImageLoaded() {
    return loadedImage != null;
  }

  @Override
  public int compareTo(GalleryItem o) {
    if (shootingTime.isPresent() || o.shootingTime.isPresent()) {
      return shootingTime.orElse(LocalDateTime.MIN).compareTo(o.shootingTime.orElse(LocalDateTime.MIN));
    } else {
      return name.compareTo(o.name);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof GalleryItem)) {
      return false;
    }

    GalleryItem that = (GalleryItem) o;

    return !(file != null ? !file.equals(that.file) : that.file != null);

  }

  @Override
  public int hashCode() {
    return file != null ? file.hashCode() : 0;
  }
}
