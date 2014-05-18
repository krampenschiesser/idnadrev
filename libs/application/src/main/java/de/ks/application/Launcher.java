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

package de.ks.application;


import javafx.application.Application;
import javafx.application.Platform;
import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class Launcher {

  private static final Logger log = LoggerFactory.getLogger(Launcher.class);
  public static final Launcher instance = new Launcher();

  private final ExecutorService executor = Executors.newCachedThreadPool();
  private final CountDownLatch latch = new CountDownLatch(2);
  private final CdiContainer cdiContainer = CdiContainerLoader.getCdiContainer();

  private Launcher() {
    //
  }

  public void start(String[] args) {
    executor.execute(() -> {
      cdiContainer.boot();
      latch.countDown();
    });
    executor.execute(() -> {
      try {

        Application.launch(App.class, args);
      } catch (Throwable t) {
        log.error("Could not launch application", t);
        stop();
        throw t;
      }
    });
    executor.execute(() -> {
      //EntityManagerProvider.getEntityManagerFactory();
      latch.countDown();
    });

  }

  public void stop() {
    boolean exceptionOccured = false;
    try {
      cdiContainer.shutdown();
    } catch (Exception e) {
      log.error("Could not stop cdi container", e);
      exceptionOccured = true;
    }
    try {
      Platform.exit();
    } catch (Exception e) {
      log.error("Could not stop javafx platform", e);
      exceptionOccured = true;
    }
    executor.shutdownNow();
    try {
      executor.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      log.error("Could not stop launching executor", e);
      exceptionOccured = true;
    }
    if (exceptionOccured) {
      System.exit(-1);
    }
  }

  public void waitForInitialization() {
    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) {
    Launcher.instance.start(args);
  }
}
