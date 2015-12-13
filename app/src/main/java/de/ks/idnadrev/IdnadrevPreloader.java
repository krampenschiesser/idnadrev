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
package de.ks.idnadrev;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class IdnadrevPreloader extends PreloaderApplication {
  @Override
  protected void startPreloader(Stage stage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("Preloader.fxml"));
    StackPane pane = loader.load();
    Preloader preloader = loader.getController();

    String versionString = Application.versioning.getVersionInfo().getVersionString();
    preloader.getVersion().setText(versionString);

    stage.setTitle("Idnadrev Version " + versionString);

    Scene scene = new Scene(pane);
    scene.getStylesheets().add("/de/ks/idnadrev/preloader.css");

    Launcher.instance.setLaunchListener(preloader);

    stage.setScene(scene);
    stage.show();
  }
}
