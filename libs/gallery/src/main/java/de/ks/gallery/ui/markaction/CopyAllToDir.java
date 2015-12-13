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
package de.ks.gallery.ui.markaction;

import de.ks.gallery.GalleryItem;
import de.ks.standbein.i18n.Localized;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class CopyAllToDir implements MarkAction {
  private static final Logger log = LoggerFactory.getLogger(CopyAllToDir.class);
  protected final Localized localized;
  protected File file;

  @Inject
  public CopyAllToDir(Localized localized) {
    this.localized = localized;
  }

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public void before(Scene scene) {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle(localized.get("choose.target.directory"));
    this.file = chooser.showDialog(scene.getWindow());
  }

  @Override
  public void execute(GalleryItem item) {
    if (file != null) {
      try {
        File targetFile = new File(this.file, item.getFile().getName());
        Files.copy(item.getFile().toPath(), targetFile.toPath());
      } catch (IOException e) {
        log.error("Could not copy {} to {}", item.getFile(), file, e);
      }
    }
  }

}
