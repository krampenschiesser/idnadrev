package de.ks.application;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.i18n.Localized;
import de.ks.imagecache.Images;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

/**
 *
 */
public class App extends Application {
  static MainWindow blurb;

  @Override
  public void start(Stage stage) throws Exception {
    Launcher.instance.waitForInitialization();

    Instance<MainWindow> select = CDI.current().select(MainWindow.class);
    if (select.isUnsatisfied()) {
      stage.setTitle(Localized.get("warning.general"));
      stage.setScene(new Scene(new Label(Localized.get("warning.unsatisfiedApplication")), 640, 480));
    } else {
      MainWindow mainWindow = select.get();
      blurb = mainWindow;
      stage.setTitle(mainWindow.getApplicationTitle());
      stage.setScene(new Scene(mainWindow.getNode()));

      Image icon = Images.get("appicon.png");
      if (icon != null) {
        stage.getIcons().add(icon);
      }
    }
    stage.setOnCloseRequest((WindowEvent e) -> {
      Launcher.instance.stop();
    });
    stage.show();
    //TODO register mainstage with navigator
  }
}
