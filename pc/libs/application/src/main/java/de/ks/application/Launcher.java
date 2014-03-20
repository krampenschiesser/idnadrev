/*
 * Copyright [2014] [Christian Loehnert]
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
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.persistence.EntityManagerProvider;
import javafx.application.Application;
import javafx.application.Platform;
import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class Launcher {
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
      Application.launch(App.class, args);
    });
    executor.execute(() -> {
      EntityManagerProvider.getEntityManagerFactory();
      latch.countDown();
    });

  }

  public void stop() {
    cdiContainer.shutdown();
    Platform.exit();
    executor.shutdownNow();
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
