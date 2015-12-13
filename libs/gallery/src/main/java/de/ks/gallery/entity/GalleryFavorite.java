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
package de.ks.gallery.entity;

import de.ks.flatadocdb.annotation.Entity;
import de.ks.flatadocdb.entity.NamedEntity;

import java.io.File;

@Entity
public class GalleryFavorite extends NamedEntity {
  private static final long serialVersionUID = 1L;
  protected String folderPath;

  protected GalleryFavorite() {
    super(null);
  }

  public GalleryFavorite(File path) {
    super(path.getName());
    folderPath = path.getAbsolutePath();
  }

  public File getFolderPath() {
    return new File(folderPath);
  }

  public void setFolderPath(File folderPath) {
    this.folderPath = folderPath.getAbsolutePath();
  }
}
