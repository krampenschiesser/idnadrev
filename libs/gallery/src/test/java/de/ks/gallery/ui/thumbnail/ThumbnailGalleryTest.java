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
package de.ks.gallery.ui.thumbnail;

import de.ks.Condition;
import de.ks.LauncherRunner;
import de.ks.activity.initialization.ActivityInitialization;
import de.ks.gallery.AbstractGalleryTest;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(LauncherRunner.class)
public class ThumbnailGalleryTest extends AbstractGalleryTest {

  @Inject
  ActivityInitialization initialization;

  @Test
  public void testLoading() throws Exception {
    ThumbnailGallery controller = initialization.loadAdditionalController(ThumbnailGallery.class).getController();
    controller.setFolder(folder, true);

    Condition.waitFor5s(() -> controller.container.getChildren(), Matchers.hasSize(6));
  }
}
