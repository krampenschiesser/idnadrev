/*
 * Copyright [2016] [Christian Loehnert]
 *
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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import de.ks.idnadrev.index.IndexModule;
import de.ks.idnadrev.repository.RepositoryModule;
import de.ks.standbein.launch.Launcher;
import de.ks.standbein.module.ApplicationModule;
import de.ks.texteditor.module.TextEditorModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;

/**
 *
 */
public class ApplicationBasic {
  private static final Logger log = LoggerFactory.getLogger(ApplicationBasic.class);

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    Locale.setDefault(Locale.GERMAN);
    HashSet<Module> modules = new HashSet<>();
    ServiceLoader<Module> loader = ServiceLoader.load(Module.class);
    loader.forEach(modules::add);
    for (Module module : modules) {
      log.info("Loaded additional module {} from service loader(classpath).", module);
    }

    modules.add(new IdnadrevModule());
    modules.add(new IndexModule());
    modules.add(new RepositoryModule());
    modules.add(new TextEditorModule());
    modules.add(new ApplicationModule());

    Injector injector = Guice.createInjector(modules);

    Launcher launcher = injector.getInstance(Launcher.class);
    launcher.launchAndWaitForUIThreads(args); //launch services

//    log.info("Starting {}, currentVersion={}, lastVersion = {}", versioning.getVersionInfo().getDescription(), versioning.getCurrentVersion(), versioning.getLastVersion());
//    versioning.upgradeToCurrentVersion();
    log.info("Finishing main");
  }

  protected static File getVersionFile() {
    String classFilePath = ApplicationBasic.class.getProtectionDomain().getCodeSource().getLocation().getFile();
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
