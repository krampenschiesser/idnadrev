package de.ks.gallery;

import com.google.common.base.StandardSystemProperty;
import de.ks.Condition;
import de.ks.DummyActivityTest;
import de.ks.FileUtil;
import de.ks.LauncherRunner;
import de.ks.activity.ActivityController;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(LauncherRunner.class)
public class GalleryResourceTest extends DummyActivityTest {
  @Inject
  GalleryResource galleryResource;
  @Inject
  ActivityController controller;

  private File folder;

  @Before
  public void setUp() throws Exception {
    Path src = getLandscapePath();

    File tmpDir = new File(StandardSystemProperty.JAVA_IO_TMPDIR.value());
    folder = new File(tmpDir, "galleryTest");

    FileUtil.deleteDir(folder);

    folder.mkdir();

    Files.copy(src, new File(folder, "r1.jpg").toPath());
    Files.copy(src, new File(folder, "r2.jpg").toPath());

    File sub = new File(folder, "sub");
    sub.mkdir();
    Files.copy(src, new File(sub, "sub1.jpg").toPath());

    File subsub = new File(sub, "subsub");
    subsub.mkdir();
    Files.copy(src, new File(subsub, "subsub1.jpg").toPath());
    Files.copy(src, new File(subsub, "subsub2.jpg").toPath());
    Files.copy(src, new File(subsub, "subsub3.jpg").toPath());
  }

  protected Path getLandscapePath() {
    URL url = getClass().getResource("landscape.jpg");
    return new File(url.getFile()).toPath();
  }

  protected Path getPortraitPath() {
    URL url = getClass().getResource("portrait.jpg");
    return new File(url.getFile()).toPath();
  }

  @Test
  public void testSimpleImageScanning() throws Exception {
    galleryResource.setFolder(folder, false);

    Condition.waitFor1s(() -> galleryResource.items.size(), Matchers.equalTo(2));

    GalleryItem galleryItem = galleryResource.items.stream().filter(i -> i.getName().contains("1")).findFirst().get();
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

    Condition.waitFor5s(() -> galleryResource.items.size(), Matchers.equalTo(6));
  }

  @Test
  public void testDeleteCreate() throws Exception {
    galleryResource.setFolder(folder, false);

    Condition.waitFor1s(() -> galleryResource.items.size(), Matchers.equalTo(2));
    GalleryItem galleryItem = galleryResource.items.stream().filter(i -> i.getName().contains("1")).findFirst().get();
    double oldWidth = galleryItem.getImage().getWidth();

    Files.copy(getPortraitPath(), new File(folder, "r1.jpg").toPath(), StandardCopyOption.REPLACE_EXISTING);

    Supplier<Double> doubleSupplier = () -> {
      Optional<GalleryItem> item = galleryResource.items.stream().filter(i -> i.getName().contains("1")).findFirst();
      if (item.isPresent()) {
        return item.get().getImage().getWidth();
      } else {
        return oldWidth;
      }
    };
    Condition.waitFor5s(doubleSupplier, Matchers.not(Matchers.equalTo(oldWidth)));
  }
}