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


import de.ks.i18n.Localized;
import de.ks.imagecache.Images;
import de.ks.javafx.FxCss;
import de.ks.launch.JavaFXService;
import de.ks.launch.Launcher;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

/**
 *
 */
public class App extends Application {
  private MainWindow mainWindow;

  @Override
  public void start(Stage stage) throws Exception {


    Instance<MainWindow> select = CDI.current().select(MainWindow.class);
    if (select.isUnsatisfied()) {
      stage.setTitle(Localized.get("warning.general"));
      stage.setScene(new Scene(new Label(Localized.get("warning.unsatisfiedApplication")), 640, 480));
    } else {
      mainWindow = select.get();
      stage.setTitle(mainWindow.getApplicationTitle());
      stage.setScene(createScene(mainWindow));

      Image icon = Images.get("appicon.png");
      if (icon != null) {
        stage.getIcons().add(icon);
      }
      Pane pane = (Pane) mainWindow.getNode();
      if (pane instanceof BorderPane) {
        Navigator.registerWithExistingPane(stage, (BorderPane) pane);
      } else {
        Navigator.register(stage, pane);
      }
    }
    stage.setOnCloseRequest((WindowEvent e) -> Launcher.instance.stopAll());
    Launcher.instance.getService(JavaFXService.class).setStage(stage);

    stage.show();
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
