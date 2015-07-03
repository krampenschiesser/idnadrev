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
package de.ks.gallery;

import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.constants.ExifTagConstants;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

public class ImageScaler {
  private static final Logger log = LoggerFactory.getLogger(ImageScaler.class);

  public Optional<LocalDateTime> getShootingTime(File image) {
    try {
      IImageMetadata metadata = Sanselan.getMetadata(image);
      if (metadata instanceof JpegImageMetadata) {
        TiffField dateTimeValue = ((JpegImageMetadata) metadata).findEXIFValue(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
        String stringValue = dateTimeValue.getStringValue().trim();

        String dateString = stringValue.split(" ")[0];
        String timeString = stringValue.split(" ")[1];

        String[] dateParts = dateString.split(":");
        String[] timeParts = timeString.split(":");

        LocalDate localDate = LocalDate.of(Integer.valueOf(dateParts[0]), Integer.valueOf(dateParts[1]), Integer.valueOf(dateParts[2]));
        LocalTime localTime = LocalTime.of(Integer.valueOf(timeParts[0]), Integer.valueOf(timeParts[1]), Integer.valueOf(timeParts[2]));

        return Optional.of(LocalDateTime.of(localDate, localTime));
      }
    } catch (Exception e) {
      log.error("Could not get metdata from {}", image, e);
    }
    return Optional.empty();
  }

  public void rotateAndWriteImage(File src, File target, int requestedSize) {
    try {
      BufferedImage image = ImageIO.read(src);
      ImageInfo imageInfo = Sanselan.getImageInfo(src);

      int orientation = getExifOrientation(src);
      if (orientation >= 0) {
        if (orientation == ExifTagConstants.ORIENTATION_VALUE_ROTATE_90_CW) {
          image = Scalr.rotate(image, Scalr.Rotation.CW_90);
        } else if (orientation == ExifTagConstants.ORIENTATION_VALUE_ROTATE_180) {
          image = Scalr.rotate(image, Scalr.Rotation.CW_180);
        } else if (orientation == ExifTagConstants.ORIENTATION_VALUE_ROTATE_270_CW) {
          image = Scalr.rotate(image, Scalr.Rotation.CW_270);
        } else if (orientation == ExifTagConstants.ORIENTATION_VALUE_MIRROR_HORIZONTAL_AND_ROTATE_90_CW) {
          image = Scalr.rotate(image, Scalr.Rotation.FLIP_HORZ);
          image = Scalr.rotate(image, Scalr.Rotation.CW_90);

        } else if (orientation == ExifTagConstants.ORIENTATION_VALUE_MIRROR_HORIZONTAL_AND_ROTATE_270_CW) {
          image = Scalr.rotate(image, Scalr.Rotation.FLIP_HORZ);
          image = Scalr.rotate(image, Scalr.Rotation.CW_270);
        } else if (orientation == ExifTagConstants.ORIENTATION_VALUE_MIRROR_HORIZONTAL) {
          image = Scalr.rotate(image, Scalr.Rotation.FLIP_HORZ);
        } else if (orientation == ExifTagConstants.ORIENTATION_VALUE_MIRROR_VERTICAL) {
          image = Scalr.rotate(image, Scalr.Rotation.FLIP_VERT);
        }
      }
      image = Scalr.resize(image, requestedSize);
      Files.createDirectories(target.getParentFile().toPath());

      String extension = imageInfo.getFormat().extension;
      ImageIO.write(image, extension, target);
    } catch (Exception e) {
      log.error("Could not add media {}", src, e);
    }
  }

  protected int getExifOrientation(File file) throws Exception {
    IImageMetadata metadata = Sanselan.getMetadata(file);
    if (metadata instanceof JpegImageMetadata) {
      TiffField orientationField = ((JpegImageMetadata) metadata).findEXIFValue(ExifTagConstants.EXIF_TAG_ORIENTATION);

      int orientation = orientationField.getIntValue();
      return orientation;
    }
    return -1;
  }
}
