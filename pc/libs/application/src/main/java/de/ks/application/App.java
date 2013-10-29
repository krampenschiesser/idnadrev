package de.ks.application;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.i18n.Localized;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

/**
 *
 */
public class App extends Application {
  @Override
  public void start(Stage stage) throws Exception {
    Launcher.instance.waitForInitialization();

    Instance<MainWindow> select = CDI.current().select(MainWindow.class);
    if (select.isUnsatisfied()) {
      stage.setTitle(Localized.get("/warning/general"));
      stage.setScene(new Scene(new Label(Localized.get("/warning/unsatisfiedApplication")), 640, 480));
    } else {
      MainWindow mainWindow = select.get();
      stage.setTitle(mainWindow.getApplicationTitle());
      stage.setScene(new Scene(mainWindow.getRoot()));
    }
    stage.setOnCloseRequest((WindowEvent e) -> {
      Launcher.instance.stop();
    });
    stage.show();
  }
}
