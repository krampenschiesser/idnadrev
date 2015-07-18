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
import de.ks.gallery.GallerySettings;
import de.ks.gallery.ImageScaler;
import de.ks.i18n.Localized;
import de.ks.option.Options;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;

import java.io.File;
import java.util.Optional;

public class CopyAllToDirAndScaleDown extends CopyAllToDir {
  private final ImageScaler imageScaler;
  private int targetWidth;

  public CopyAllToDirAndScaleDown() {
    imageScaler = new ImageScaler();
  }

  @Override
  public void before(Scene scene) {
    super.before(scene);
    int scaleDownSize = Options.get(GallerySettings.class).getScaleDownSize();
    TextInputDialog dialog = new TextInputDialog(String.valueOf(scaleDownSize));
    dialog.setTitle(Localized.get("enter.targetResolution"));
    dialog.setHeaderText(null);
    dialog.setContentText(Localized.get("targetResolution:"));
    Optional<String> s = dialog.showAndWait();
    if (s.isPresent()) {
      try {
        int i = Integer.parseInt(s.get());
        targetWidth = i;
      } catch (NumberFormatException e) {
        targetWidth = 0;
      }
    } else {
      targetWidth = 0;
    }
  }

  @Override
  public void execute(GalleryItem item) {
    if (targetWidth > 0) {
      if (file != null) {
        File targetFile = new File(this.file, item.getFile().getName());
        imageScaler.rotateAndWriteImage(item.getFile(), targetFile, targetWidth);
      }
    }
  }
}
