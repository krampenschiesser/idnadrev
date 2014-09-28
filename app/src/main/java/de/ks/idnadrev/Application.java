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

package de.ks.idnadrev;

import de.ks.launch.JavaFXService;
import de.ks.launch.Launcher;
import de.ks.version.Versioning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 *
 */
public class Application {
  private static final Logger log = LoggerFactory.getLogger(Application.class);
  public static final Versioning versioning = new Versioning(getVersionFile(), Application.class);

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    log.info("Starting {}, currentVersion={}, lastVersion = ", versioning.getVersionInfo().getDescription(), versioning.getCurrentVersion(), versioning.getLastVersion());
    versioning.upgradeToCurrentVersion();

    Launcher launcher = Launcher.instance;
    launcher.setPreloader(IdnadrevPreloader.class);
    launcher.startAll(args);
    launcher.awaitStart();
    launcher.getService(JavaFXService.class).waitUntilFXFinished();
    launcher.stopAll();
    launcher.awaitStop();
    log.info("Finishing main");
  }

  protected static File getVersionFile() {
    String classFilePath = Application.class.getProtectionDomain().getCodeSource().getLocation().getFile();
    File file = new File(classFilePath);
    return new File(discoverParentDir(file), "version.info");
  }

  private static File discoverParentDir(File file) {
    File parent = null;
    for (parent = file; !parent.isDirectory() || parent.getPath().contains("build"); parent = parent.getParentFile()) {
    }
    return parent;
  }
}
