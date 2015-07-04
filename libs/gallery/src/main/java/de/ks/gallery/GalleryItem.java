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

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import org.apache.sanselan.ImageReadException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

public class GalleryItem implements Comparable<GalleryItem> {
  protected final File file;
  protected final String name;
  protected final Image image;
  protected final Image thumbNail;
  protected final Optional<LocalDateTime> shootingTime;

  public GalleryItem(File file, int thumbnailSize) throws IOException, ImageReadException {
    if (file == null) {
      throw new IllegalArgumentException("Cannot create from null file ");
    }
    if (!file.exists()) {
      throw new IllegalArgumentException("Cannot create from non-existing file " + file);
    }
    this.file = file;

    ImageScaler scaler = new ImageScaler();
    BufferedImage originalRotated = scaler.rotate(file);
    BufferedImage thumbNailImage = scaler.rotateAndScale(file, thumbnailSize);

    this.image = SwingFXUtils.toFXImage(originalRotated, new WritableImage(originalRotated.getWidth(), originalRotated.getHeight()));
    this.thumbNail = SwingFXUtils.toFXImage(thumbNailImage, new WritableImage(thumbNailImage.getWidth(), thumbNailImage.getHeight()));
    name = file.getName();

    shootingTime = scaler.getShootingTime(file);
  }

  public File getFile() {
    return file;
  }

  public String getName() {
    return name;
  }

  public Image getImage() {
    return image;
  }

  public Image getThumbNail() {
    return thumbNail;
  }

  public Optional<LocalDateTime> getShootingTime() {
    return shootingTime;
  }

  @Override
  public int compareTo(GalleryItem o) {
    if (shootingTime.isPresent() && o.shootingTime.isPresent()) {
      return shootingTime.get().compareTo(o.shootingTime.get());
    } else {
      return name.compareTo(o.name);
    }
  }
}
