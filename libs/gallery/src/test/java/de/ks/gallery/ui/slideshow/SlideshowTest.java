package de.ks.gallery.ui.slideshow;

import de.ks.gallery.AbstractGalleryTest;
import de.ks.gallery.GalleryResource;
import de.ks.standbein.Condition;
import de.ks.standbein.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.activity.initialization.ActivityInitialization;
import de.ks.util.FXPlatform;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.*;

public class SlideshowTest extends AbstractGalleryTest {

  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IntegrationTestModule()).launchServices();
  Slideshow slideshow;

  @Inject
  GalleryResource resource;
  @Inject
  ActivityInitialization initialization;

  @Test
  public void testNextPreloading() throws Exception {
    slideshow = initialization.loadAdditionalController(Slideshow.class).getController();
    resource.setCallback(items -> slideshow.getItems().addAll(items));
    resource.setFolder(folder, true);

    Condition.waitFor5s(() -> slideshow.sorted, Matchers.hasSize(6));

    FXPlatform.invokeLater(() -> slideshow.show(0));//index=0
    FXPlatform.invokeLater(() -> slideshow.next());//index=1
    assertTrue(slideshow.sorted.get(0).isImageLoaded());
    assertTrue(slideshow.sorted.get(1).isImageLoaded());
    Condition.waitFor1s("Image not loaded", () -> slideshow.sorted.get(2).isImageLoaded());

    FXPlatform.invokeLater(() -> slideshow.next());//index=2
    assertFalse(slideshow.sorted.get(0).isImageLoaded());//now unloaded
    assertTrue(slideshow.sorted.get(1).isImageLoaded());
    assertTrue(slideshow.sorted.get(2).isImageLoaded());
    Condition.waitFor1s("Image not loaded", () -> slideshow.sorted.get(3).isImageLoaded());

  }

  @Test
  public void testPreviousPreloading() throws Exception {
    slideshow = initialization.loadAdditionalController(Slideshow.class).getController();
    resource.setCallback(items -> slideshow.getItems().addAll(items));
    resource.setFolder(folder, true);

    Condition.waitFor5s(() -> slideshow.sorted, Matchers.hasSize(6));

    FXPlatform.invokeLater(() -> slideshow.show(5));//index=5
    FXPlatform.invokeLater(() -> slideshow.previous());//index=4
    assertTrue(slideshow.sorted.get(5).isImageLoaded());
    assertTrue(slideshow.sorted.get(4).isImageLoaded());
    Condition.waitFor1s("Image not loaded", () -> slideshow.sorted.get(3).isImageLoaded());

    FXPlatform.invokeLater(() -> slideshow.previous());//index=3
    assertFalse(slideshow.sorted.get(5).isImageLoaded());//now unloaded
    assertTrue(slideshow.sorted.get(4).isImageLoaded());
    assertTrue(slideshow.sorted.get(3).isImageLoaded());
    Condition.waitFor1s("Image not loaded", () -> slideshow.sorted.get(2).isImageLoaded());
  }

  @Test
  public void test2Items() throws Exception {
    slideshow = initialization.loadAdditionalController(Slideshow.class).getController();

    resource.setCallback(items -> slideshow.getItems().addAll(items));
    resource.setFolder(folder, false);

    Condition.waitFor5s(() -> slideshow.sorted, Matchers.hasSize(2));

    FXPlatform.invokeLater(() -> slideshow.show(0));//index=0
    FXPlatform.invokeLater(() -> slideshow.next());//index=1
    assertTrue(slideshow.sorted.get(0).isImageLoaded());
    assertTrue(slideshow.sorted.get(1).isImageLoaded());

    FXPlatform.invokeLater(() -> slideshow.next());//index=0
    assertTrue(slideshow.sorted.get(0).isImageLoaded());
    assertTrue(slideshow.sorted.get(1).isImageLoaded());


    FXPlatform.invokeLater(() -> slideshow.previous());//index=1
    assertTrue(slideshow.sorted.get(0).isImageLoaded());
    assertTrue(slideshow.sorted.get(1).isImageLoaded());

    FXPlatform.invokeLater(() -> slideshow.previous());//index=0
    assertTrue(slideshow.sorted.get(0).isImageLoaded());
    assertTrue(slideshow.sorted.get(1).isImageLoaded());
  }

  @Test
  public void test1Item() throws Exception {
    slideshow = initialization.loadAdditionalController(Slideshow.class).getController();

    resource.setCallback(items -> slideshow.getItems().addAll(items));
    resource.setFolder(sub, false);

    Condition.waitFor5s(() -> slideshow.sorted, Matchers.hasSize(1));

    FXPlatform.invokeLater(() -> slideshow.show(0));//index=0
    FXPlatform.invokeLater(() -> slideshow.next());//index=0
    assertTrue(slideshow.sorted.get(0).isImageLoaded());

    FXPlatform.invokeLater(() -> slideshow.next());//index=0
    assertTrue(slideshow.sorted.get(0).isImageLoaded());

    FXPlatform.invokeLater(() -> slideshow.previous());//index=0
    assertTrue(slideshow.sorted.get(0).isImageLoaded());

    FXPlatform.invokeLater(() -> slideshow.previous());//index=0
    assertTrue(slideshow.sorted.get(0).isImageLoaded());
  }
}