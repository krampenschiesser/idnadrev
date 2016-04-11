/*
 * Copyright [2016] [Christian Loehnert]
 *
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
package de.ks.idnadrev.util;

import de.ks.standbein.activity.ActivityController;
import de.ks.standbein.i18n.Localized;
import de.ks.standbein.imagecache.Images;
import de.ks.standbein.validation.ValidationRegistry;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

import javax.inject.Inject;

public class ButtonHelper {
  private final Images images;
  private final Localized localized;
  private final ActivityController activityController;
  private final ValidationRegistry validationRegistry;

  @Inject
  public ButtonHelper(Images images, Localized localized, ActivityController activityController, ValidationRegistry validationRegistry) {
    this.images = images;
    this.localized = localized;
    this.activityController = activityController;
    this.validationRegistry = validationRegistry;
  }

  public Button createSaveButton(boolean stopActivity) {
    Button save = createImageButton("save", "save.png", 24);
    save.setDefaultButton(true);
    save.setOnAction(e -> {
      activityController.save();
      if (stopActivity) {
        activityController.stopCurrent();
      }
    });
    save.disableProperty().bind(validationRegistry.invalidProperty());
    return save;
  }

  public Button createImageButton(String title, String image, int iconSize) {
    ImageView imageView = new ImageView(images.get(image));
    imageView.setFitHeight(iconSize);
    imageView.setFitWidth(iconSize);
    return new Button(title, imageView);
  }
}
