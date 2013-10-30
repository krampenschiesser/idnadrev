package de.ks;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class JFXCDIRunner extends BlockJUnit4ClassRunner {
  public static class JFXTestApp extends Application {
    public static Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
      barrier.countDown();
      JFXTestApp.stage = stage;
    }
  }

  private static final ExecutorService executor = Executors.newCachedThreadPool();
  private static CountDownLatch barrier = new CountDownLatch(2);
  private final CdiContainer cdiContainer = CdiContainerLoader.getCdiContainer();

  public JFXCDIRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override
  public void run(RunNotifier notifier) {
    startApp();
    super.run(notifier);
    stopApp();
  }


  public void startApp() {
    if (barrier.getCount() == 2) {
      executor.execute(() -> {
        Application.launch(JFXTestApp.class);
      });
    }
    executor.execute(() -> {
      try {
        cdiContainer.boot();
      } finally {
        barrier.countDown();
      }
    });
    try {
      if (!barrier.await(5, TimeUnit.SECONDS)) {
        stopApp();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public void stopApp() {
//    Platform.exit();
    cdiContainer.shutdown();
    barrier = new CountDownLatch(1);
  }

}
