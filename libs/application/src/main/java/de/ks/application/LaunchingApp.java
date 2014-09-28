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

package de.ks.application;

import de.ks.i18n.Localized;
import de.ks.imagecache.Images;
import de.ks.javafx.FxCss;
import de.ks.launch.ApplicationService;
import de.ks.launch.Launcher;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

/**
 *
 */
public class LaunchingApp extends Application {
  private static final Logger log = LoggerFactory.getLogger(LaunchingApp.class);

  @Override
  public void start(Stage stage) throws Exception {
    try {
      log.info("Starting application " + getClass().getName());
      stage.setTitle(Localized.get("warning.general"));
      stage.setScene(new Scene(new Label(Localized.get("warning.unsatisfiedApplication")), 640, 480));

      Image icon = Images.get("appicon.png");
      if (icon != null) {
        stage.getIcons().add(icon);
      }
//      if (pane instanceof BorderPane) {
//        Navigator.registerWithExistingPane(stage, (BorderPane) pane);
//      } else {
//        Navigator.register(stage, pane);
//      }
      stage.setOnCloseRequest((WindowEvent e) -> {
        Launcher.instance.stopAll();
        Launcher.instance.awaitStop();
      });
      Launcher.instance.getService(ApplicationService.class).setStage(stage);
      stage.show();
    } catch (Exception e) {
      log.error("Could not start JavaFXApp", e);
      throw e;
    }
  }

  private Scene createScene(MainWindow mainWindow) {
    Scene scene = new Scene(mainWindow.getNode());
    Instance<String> styleSheets = CDI.current().select(String.class, FxCss.LITERAL);
    styleSheets.forEach((sheet) -> {
      scene.getStylesheets().add(sheet);
    });
    return scene;
  }
}
