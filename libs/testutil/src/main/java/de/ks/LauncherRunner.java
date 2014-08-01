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
package de.ks;

import de.ks.launch.Launcher;
import de.ks.version.Versioning;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import java.io.File;

public class LauncherRunner extends BlockJUnit4ClassRunner {
  private static final Logger log = LoggerFactory.getLogger(LauncherRunner.class);

  public LauncherRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override
  public void run(RunNotifier notifier) {
    File versionFile = new File(System.getProperty("user.dir"), "version.info");
    Versioning versioning = new Versioning(versionFile, LauncherRunner.class);
    versioning.upgradeToCurrentVersion();

    Launcher launcher = Launcher.instance;
    if (!launcher.isStarted()) {
      launcher.startAll();
      launcher.awaitStart();
    }
    notifier.addListener(new RunListener() {
      @Override
      public void testStarted(Description description) throws Exception {
        super.testStarted(description);
        log.info("@Test:{}.{}", description.getClassName(), description.getMethodName());
      }
    });
    super.run(notifier);
  }

  @Override
  protected Object createTest() throws Exception {
    Instance<?> select = CDI.current().select(getTestClass().getJavaClass());
    if (select.isUnsatisfied()) {
      return getTestClass().getJavaClass().newInstance();
    } else {
      return select.get();
    }
  }
}
