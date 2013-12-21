package de.ks.imagecache;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javafx.scene.image.Image;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;


/**
 *
 */
@SuppressWarnings("SpellCheckingInspection")
public class ImagesTest {
  private static final Logger log = LoggerFactory.getLogger(ImagesTest.class);
  private String packageImage = "packageimage.png";
  private String imageFolderImage = "imageFolderImage.png";
  private String fileImage;

  @Before
  public void setUp() throws Exception {
    String workingDirectory = System.getProperty("user.dir");
    log.info("working in {}", workingDirectory);
    if (workingDirectory.endsWith("images")) {
      fileImage = "../../libs/images/fileimage.jpg";
    } else {
      fileImage = "pc/libs/images/fileimage.jpg";
    }

  }

  @Test
  public void testFindImages() throws Exception {
    assertNotNull(Images.get(fileImage));
    assertNotNull(Images.get(packageImage));
    assertNotNull(Images.get(imageFolderImage));
  }

  @Test
  public void testAsyncImage() throws Exception {
    Images.later(fileImage, (Image img) -> assertNotNull(img));
  }
}
