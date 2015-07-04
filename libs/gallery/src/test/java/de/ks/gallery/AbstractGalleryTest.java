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
package de.ks.gallery;

import com.google.common.base.StandardSystemProperty;
import de.ks.DummyActivityTest;
import de.ks.FileUtil;
import org.junit.Before;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class AbstractGalleryTest extends DummyActivityTest {

  protected File folder;

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
    URL url = AbstractGalleryTest.class.getResource("landscape.jpg");
    return new File(url.getFile()).toPath();
  }

  protected Path getPortraitPath() {
    URL url = AbstractGalleryTest.class.getResource("portrait.jpg");
    return new File(url.getFile()).toPath();
  }

}
