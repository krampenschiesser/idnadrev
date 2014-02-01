package de.ks.fxtest;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.JFXCDIRunner;
import de.ks.application.fxml.DefaultLoader;
import javafx.scene.layout.StackPane;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;

/**
 *
 */
@RunWith(JFXCDIRunner.class)
public class FxTest {
  private static final Logger log = LoggerFactory.getLogger(FxTest.class);

  @Test
  public void testName() throws Exception {
    Profiler profiler = new Profiler("fxml loading of the same file");
    profiler.setLogger(log);
    for (int i = 0; i < 1000; i++) {
      DefaultLoader<StackPane, FxTestController> loader = new DefaultLoader<>(FxTestController.class);
      loader.getController();
    }
    profiler.stop().log();
    log.info("Average time: {}ms", profiler.elapsedTime() / 1000 / 1000 / 1000);
  }
}
