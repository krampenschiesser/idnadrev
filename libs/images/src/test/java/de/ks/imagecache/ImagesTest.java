/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
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

package de.ks.imagecache;

import javafx.scene.image.Image;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ForkJoinPool;

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
    Image image = Images.later(fileImage, ForkJoinPool.commonPool()).get();
    assertNotNull(image);
  }
}
