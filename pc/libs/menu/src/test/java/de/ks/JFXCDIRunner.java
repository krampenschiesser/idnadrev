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

/**
 *
 */
public class JFXCDIRunner extends BlockJUnit4ClassRunner {
  public static class JFXTestApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
      barrier.countDown();
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
      cdiContainer.boot();
      barrier.countDown();
    });
    try {
      barrier.await();
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
