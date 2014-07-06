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

import com.google.common.cache.CacheLoader;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 */
public class ImageLoader extends CacheLoader<String, Image> {
  public static final String DEFAULT_IMAGE_PACKAGE = "/de/ks/images/";

  private static final Logger log = LoggerFactory.getLogger(ImageLoader.class);

  @Override
  public Image load(String key) throws Exception {
    URL resource = getClass().getResource(key);
    if (resource == null) {
      log.debug("Could not load image {} from classpath", key);
    } else {
      Image image = loadFromUrl(resource);
      log.info("Loaded image {} from classpath", key);
      return image;
    }
    resource = getClass().getResource(DEFAULT_IMAGE_PACKAGE + key);
    if (resource == null) {
      log.debug("Could not load image {} from default image package {}", key, DEFAULT_IMAGE_PACKAGE);
    } else {
      Image image = loadFromUrl(resource);
      log.info("Loaded image {} from default image package {}", key, DEFAULT_IMAGE_PACKAGE);
      return image;
    }

    File file = new File(key);
    if (file.exists()) {

      Image image = loadFromFile(file);
      log.info("Loaded image {} from file {}", key, file);
      return image;
    } else {
      log.debug("Could not load image {} from filesystem", key);
    }
    try {
      URL url = new URL(key);
      Image image = loadFromUrl(url);
      log.info("Loaded image {} from url {}", key, url);
      return image;
    } catch (MalformedURLException e) {
      log.debug("Could not load image {} via URL", key);
    }
    throw new FileNotFoundException(key);
  }

  private Image loadFromUrl(URL url) {
    try (InputStream stream = url.openStream()) {
      return new Image(stream);
    } catch (IOException e) {
      log.error("Could not load from stream {}", url, e);
      return null;
    }
  }

  private Image loadFromFile(File file) {
    try (FileInputStream stream = new FileInputStream(file)) {
      return new Image(stream);
    } catch (IOException e) {
      log.error("Could not load from file {}", file.getPath(), e);
      return null;
    }
  }
}
