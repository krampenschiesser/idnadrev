package de.ks.gallery;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class GalleryResourceTest extends AbstractGalleryTest {
  @Inject
  GalleryResource galleryResource;
  @Inject
  ActivityController controller;
  @Test
  public void testSimpleImageScanning() throws Exception {
    galleryResource.setFolder(folder, false);

    Condition.waitFor1s(() -> galleryResource.getItems().size(), Matchers.equalTo(2));

    GalleryItem galleryItem = galleryResource.getItems().stream().filter(i -> i.getName().contains("1")).findFirst().get();
    assertNotNull(galleryItem.getFile());
    assertNotNull(galleryItem.getName());
    assertNotNull(galleryItem.getImage());
    assertNotNull(galleryItem.getThumbNail());

    double max = Math.max(galleryItem.getThumbNail().getWidth(), galleryItem.getThumbNail().getHeight());
    assertEquals(galleryResource.getThumbnailSize(), max, 0.001);
  }

  @Test
  public void testRecursion() throws Exception {
    galleryResource.setFolder(folder, true);

    Condition.waitFor5s(() -> galleryResource.getItems().size(), Matchers.equalTo(6));
  }

  @Test
  public void testDeleteCreate() throws Exception {
    galleryResource.setFolder(folder, false);

    Condition.waitFor1s(() -> galleryResource.getItems().size(), Matchers.equalTo(2));
    GalleryItem galleryItem = galleryResource.getItems().stream().filter(i -> i.getName().contains("1")).findFirst().get();
    double oldWidth = galleryItem.getImage().getWidth();

    Files.copy(getPortraitPath(), new File(folder, "r1.jpg").toPath(), StandardCopyOption.REPLACE_EXISTING);

    Supplier<Double> doubleSupplier = () -> {
      Optional<GalleryItem> item = galleryResource.getItems().stream().filter(i -> i.getName().contains("1")).findFirst();
      if (item.isPresent()) {
        return item.get().getImage().getWidth();
      } else {
        return oldWidth;
      }
    };
    Condition.waitFor5s(doubleSupplier, Matchers.not(Matchers.equalTo(oldWidth)));
  }
}