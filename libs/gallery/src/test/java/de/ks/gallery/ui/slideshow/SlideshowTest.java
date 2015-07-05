package de.ks.gallery.ui.slideshow;

import de.ks.Condition;
import de.ks.LauncherRunner;
import de.ks.gallery.AbstractGalleryTest;
import de.ks.gallery.GalleryResource;
import de.ks.util.FXPlatform;
import javafx.beans.binding.Bindings;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(LauncherRunner.class)
public class SlideshowTest extends AbstractGalleryTest {

  Slideshow slideshow;

  @Inject
  GalleryResource resource;

  @Test
  public void testNextPreloading() throws Exception {
    slideshow = FXPlatform.invokeLater(() -> CDI.current().select(Slideshow.class).get());
    Bindings.bindContent(slideshow.getItems(), resource.getItems());
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
    slideshow = FXPlatform.invokeLater(() -> CDI.current().select(Slideshow.class).get());
    Bindings.bindContent(slideshow.getItems(), resource.getItems());
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
    slideshow = FXPlatform.invokeLater(() -> CDI.current().select(Slideshow.class).get());

    Bindings.bindContent(slideshow.getItems(), resource.getItems());
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
    slideshow = FXPlatform.invokeLater(() -> CDI.current().select(Slideshow.class).get());

    Bindings.bindContent(slideshow.getItems(), resource.getItems());
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