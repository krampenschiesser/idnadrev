package de.ks.blogging.grav.posts.media;

import com.google.common.base.StandardSystemProperty;
import de.ks.blogging.grav.GravSettings;
import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.Sanselan;
import org.junit.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImageScalerTest {
  @Test
  public void testCopyImage() throws Exception {
    GravSettings settings = new GravSettings();
    ImageScaler imageScaler = new ImageScaler();

    File landscapeFile = new File(getClass().getResource("landscape.jpg").getFile());
    File portraitFile = new File(getClass().getResource("portrait.jpg").getFile());

    File landscapeTarget = new File(new File(StandardSystemProperty.JAVA_IO_TMPDIR.value()), "landscape.jpg");
    File portraitTarget = new File(new File(StandardSystemProperty.JAVA_IO_TMPDIR.value()), "portrait.jpg");

    imageScaler.rotateAndWriteImage(landscapeFile, landscapeTarget, 1024);
    imageScaler.rotateAndWriteImage(portraitFile, portraitTarget, 1024);

    assertTrue(landscapeTarget.exists());
    assertTrue(portraitTarget.exists());

    ImageInfo imageInfo = Sanselan.getImageInfo(landscapeTarget);
    assertEquals(1024, imageInfo.getWidth());

    imageInfo = Sanselan.getImageInfo(portraitTarget);
    assertEquals(768, imageInfo.getWidth());
    assertEquals(1024, imageInfo.getHeight());
  }

  @Test
  public void testGetShootingDate() throws Exception {
    ImageScaler imageScaler = new ImageScaler();
    File landscapeFile = new File(getClass().getResource("landscape.jpg").getFile());
    File portraitFile = new File(getClass().getResource("portrait.jpg").getFile());

    LocalDateTime landscapeTime = LocalDateTime.of(2013, 4, 19, 9, 41, 33);
    LocalDateTime portraitTime = LocalDateTime.of(2013, 4, 20, 18, 48, 52);

    Optional<LocalDateTime> shootingTime = imageScaler.getShootingTime(landscapeFile);
    assertEquals(landscapeTime, shootingTime.get());

    shootingTime = imageScaler.getShootingTime(portraitFile);
    assertEquals(portraitTime, shootingTime.get());

  }
}