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
