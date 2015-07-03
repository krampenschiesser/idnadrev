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

import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import org.apache.sanselan.formats.gif.ImageDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;
import java.util.Collection;

public class GalleryResource {
  private static final Logger log = LoggerFactory.getLogger(GalleryResource.class);

  private final WatchService watchService;

  public static GalleryResource fromFiles(Collection<File> files) {
    return new GalleryResource();
  }

  public static GalleryResource fromFolder(File folder, boolean recurse) {
    return new GalleryResource();
  }

  public static GalleryResource fromImages(Collection<Image> images) {
    return new GalleryResource();
  }

  private GalleryResource() {
    WatchService service;
    try {
      service = FileSystems.getDefault().newWatchService();
    } catch (IOException e) {
      service = null;
    }
    watchService = service;
  }

  public boolean isVirtualImageNames() {
    return false;//if we only use javafx images
  }

  public ObservableList<Image> getImages() {
    return null;
  }

  public ObservableList<String> getImageNames() {
    return null;
  }

  public ObservableList<Image> getThumbnails() {
    return null;
  }

  public ObservableList<ImageDescriptor> getImageDescriptors() {
    return null;
  }

}
