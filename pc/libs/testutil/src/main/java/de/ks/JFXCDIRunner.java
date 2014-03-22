/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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

package de.ks;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class JFXCDIRunner extends CDIRunner {
  private static final Logger log = LoggerFactory.getLogger(JFXCDIRunner.class);

  private static final CountDownLatch barrier = new CountDownLatch(1);

  public static Stage getStage() {
    return JFXTestApp.stage;
  }

  public static class JFXTestApp extends Application {
    public static Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
      barrier.countDown();
      JFXTestApp.stage = stage;
    }
  }

  public JFXCDIRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override
  protected void start() {
    super.start();
    if (barrier.getCount() == 1) {
      executor.execute(() -> {
        try {
          Application.launch(JFXTestApp.class);
        } finally {
          barrier.countDown();
        }
      });
    }
  }

  @Override
  protected void await() {
    super.await();
    try {
      if (!barrier.await(5, TimeUnit.SECONDS)) {
        stop();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    super.stop();
    Platform.exit();
  }
}
