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
package de.ks.gallery.ui;

import de.ks.flatjsondb.PersistentWork;
import de.ks.gallery.AbstractGalleryTest;
import de.ks.gallery.GalleryItem;
import de.ks.gallery.entity.GalleryFavorite;
import de.ks.gallery.ui.slideshow.Slideshow;
import de.ks.standbein.Condition;
import de.ks.standbein.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.activity.ActivityCfg;
import de.ks.util.FXPlatform;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.GridPane;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import java.io.File;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class GalleryTest extends AbstractGalleryTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IntegrationTestModule()).launchServices();
  @Inject
  PersistentWork work;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return GalleryActivity.class;
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void testFileSelection() throws Exception {
    GalleryController controller = activityController.getControllerInstance(GalleryController.class);

    FXPlatform.invokeLater(() -> controller.selectPath(subsub));

    File selectedFile = controller.fileView.getSelectionModel().getSelectedItem().getValue();
    assertEquals(subsub, selectedFile);
  }

  @Test
  public void testFavorites() throws Exception {
    GalleryController controller = activityController.getControllerInstance(GalleryController.class);

    work.removeAllOf(GalleryFavorite.class);
    work.persist(new GalleryFavorite(sub));
    controller.reloadFavorites();

    Condition.waitFor5s(() -> controller.favoriteContainer.getChildren(), Matchers.hasSize(1));

    GridPane pane = (GridPane) controller.favoriteContainer.getChildren().get(0);
    Hyperlink link = (Hyperlink) pane.getChildren().get(0);
    Button delete = (Button) pane.getChildren().get(1);
    assertEquals("sub", link.getText());

    FXPlatform.invokeLater(() -> delete.getOnAction().handle(null));
    Condition.waitFor5s(() -> controller.favoriteContainer.getChildren(), Matchers.hasSize(0));

    FXPlatform.invokeLater(() -> controller.selectPath(sub));
    FXPlatform.invokeLater(() -> controller.onMarkFavorite());

    Condition.waitFor5s(() -> controller.favoriteContainer.getChildren(), Matchers.hasSize(1));
  }

  @Test
  public void testMarked() throws Exception {
    GalleryController controller = activityController.getControllerInstance(GalleryController.class);
    Slideshow slideshow = controller.thumbnailGallery.getSlideshow();

    FXPlatform.invokeLater(() -> controller.selectPath(subsub));

    Condition.waitFor5s(() -> controller.fileView.getSelectionModel().getSelectedItem(), Matchers.notNullValue());
    Condition.waitFor5s(() -> controller.thumbnailGallery.getAllThumbNails(), Matchers.hasSize(3));

    FXPlatform.invokeLater(() -> slideshow.getMarkedItems().addAll(controller.thumbnailGallery.getAllThumbNails().stream().map(t -> t.getItem()).collect(Collectors.toList())));
    Condition.waitFor1s(() -> slideshow.getMarkedItems(), Matchers.hasSize(3));

    assertEquals(3, controller.markedItemController.markedTable.getItems().size());
  }

  @Test
  public void testDeleteMarked() throws Exception {
    GalleryController controller = activityController.getControllerInstance(GalleryController.class);
    Slideshow slideshow = controller.thumbnailGallery.getSlideshow();

    FXPlatform.invokeLater(() -> controller.selectPath(subsub));

    Condition.waitFor5s(() -> controller.fileView.getSelectionModel().getSelectedItem(), Matchers.notNullValue());
    Condition.waitFor5s(() -> controller.thumbnailGallery.getAllThumbNails(), Matchers.hasSize(3));

    FXPlatform.invokeLater(() -> slideshow.getMarkedForDeletion().addAll(controller.thumbnailGallery.getAllThumbNails().stream().map(t -> t.getItem()).limit(2).collect(Collectors.toList())));
    Condition.waitFor1s(() -> slideshow.getMarkedForDeletion(), Matchers.hasSize(2));

    GalleryItem itemToDelete = slideshow.getMarkedForDeletion().get(0);
    FXPlatform.invokeLater(() -> controller.markedItemController.onDeleteMarkedForDeletion());

    Condition.waitFor1s(() -> slideshow.getMarkedForDeletion(), Matchers.hasSize(0));
    assertFalse(itemToDelete.getFile().exists());

    Condition.waitFor5s(() -> controller.thumbnailGallery.getAllThumbNails(), Matchers.hasSize(1));//one remaining
  }
}
