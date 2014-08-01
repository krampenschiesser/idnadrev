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
package de.ks.version;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;

public class VersionTest {
  private static final Logger log = LoggerFactory.getLogger(VersionTest.class);
  public static AtomicLong upgradeCounter = new AtomicLong();

  @Test
  public void testReadFromMetaInf() throws Exception {
    VersionInfo versionInfo = new VersionInfo(getClass());
    log.info(versionInfo.getDescription());
    int versionNumber = versionInfo.getVersion();
    assertEquals(23, versionNumber);
  }

  @Test
  public void testWriteVersion() throws Exception {
    File testversion = getVersionFile();
    Versioning versioning = new Versioning(testversion, VersionTest.class);
    assertEquals(-1, versioning.getLastVersion());
    versioning.writeLastVersion(230);
    assertEquals(230, versioning.getLastVersion());
  }

  protected File getVersionFile() {
    File tempDir = new File(System.getProperty("java.io.tmpdir"));
    File testversion = new File(tempDir, "testversion");
    if (testversion.exists()) {
      testversion.delete();
    }
    return testversion;
  }

  @Test
  public void testUpgrade() throws Exception {
    upgradeCounter.set(0);
    Versioning versioning = new Versioning(getVersionFile(), VersionTest.class) {
      @Override
      public int getLastVersion() {
        return 1;
      }

      @Override
      public int getCurrentVersion() {
        return 3;
      }
    };

    versioning.upgradeToCurrentVersion();
    assertEquals(2, upgradeCounter.get());
  }
}
