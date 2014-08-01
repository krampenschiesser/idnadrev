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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.ks.SubclassInstantiator;
import de.ks.launch.Launcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Versioning {
  private static final Logger log = LoggerFactory.getLogger(Versioning.class);
  public static final String VERSION_PACKAGES = "version.packages";
  public static final String VERSION_PROPERTIES_FILENAME = "version.properties";
  public static final String PACKAGE_SEPARATOR = Launcher.PACKAGE_SEPARATOR;
  public static final String APP_VERSION = "app.version";

  private final ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("versioning-%d").build());
  private final File versionFile;
  private final Class<?> owner;
  private final VersionInfo versionInfo;
  private final SubclassInstantiator instantiator;

  public Versioning(File versionFile, Class<?> owner) {
    this.versionFile = versionFile;
    this.owner = owner;
    versionInfo = new VersionInfo(owner);
    executorService.setKeepAliveTime(1, TimeUnit.SECONDS);
    instantiator = new SubclassInstantiator(executorService, getClass().getPackage(), VERSION_PROPERTIES_FILENAME, VERSION_PACKAGES, PACKAGE_SEPARATOR);
  }

  public boolean isInitialImport() {
    return getLastVersion() == -1;
  }

  public int getCurrentVersion() {
    return versionInfo.getVersion();
  }

  public VersionInfo getVersionInfo() {
    return versionInfo;
  }

  public int getLastVersion() {
    try (FileInputStream stream = new FileInputStream(versionFile)) {
      Properties properties = new Properties();
      properties.load(stream);
      String value = properties.getProperty(APP_VERSION, "-1");
      return Integer.valueOf(value);
    } catch (IOException e) {
      log.warn("No {} present, will assume version -1", versionFile);
      return -1;
    }
  }

  public void writeLastVersion(int version) {
    if (!versionFile.exists()) {
      try {
        versionFile.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    try (FileOutputStream fileOutputStream = new FileOutputStream(versionFile)) {
      Properties properties = new Properties();
      properties.setProperty(APP_VERSION, String.valueOf(version));
      properties.store(fileOutputStream, "42");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void upgradeToCurrentVersion() {
    if (isInitialImport()) {
      getInitialImportRunners().forEach(r -> r.performInitialImport());
      writeLastVersion(getCurrentVersion());
    } else if (getLastVersion() < getCurrentVersion()) {
      List<VersionUpgrade> upgraders = getVersionUpgrades();
      Collections.sort(upgraders);

      upgraders.stream().filter(upgrader -> upgrader.getVersion() > getLastVersion()).forEach(upgrader -> {
        log.info("Performing upgrade for version {} using {}", upgrader.getVersion(), upgrader.getClass().getSimpleName());
        upgrader.performUpgrade();
      });
      writeLastVersion(getCurrentVersion());
    } else {
      log.debug("no upgrade nessessary");
    }
  }

  protected List<InitialImport> getInitialImportRunners() {
    List<InitialImport> importers = instantiator.instantiateSubclasses(InitialImport.class);
    log.debug("Found {} initial importers: {}", importers.size(), importers);
    return importers;
  }

  protected List<VersionUpgrade> getVersionUpgrades() {
    List<VersionUpgrade> upgraders = instantiator.instantiateSubclasses(VersionUpgrade.class);
    log.debug("Found {} initial importers: {}", upgraders.size(), upgraders);
    return upgraders;

  }
}
