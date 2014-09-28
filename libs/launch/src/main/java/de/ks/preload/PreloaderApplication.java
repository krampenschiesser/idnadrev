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
package de.ks.preload;

import de.ks.launch.Launcher;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PreloaderApplication extends Application {
  private static final Logger log = LoggerFactory.getLogger(PreloaderApplication.class);
  private Stage stage;

  @Override
  public void start(Stage primaryStage) throws Exception {
    stage = primaryStage;
    try {
      startPreloader(stage);
      Launcher.instance.setPreloaderInstance(this);
    } catch (Exception e) {
      Launcher.instance.setPreloaderInstance(null);
      log.error("Could not start preloader {}", getClass().getName(), e);
      throw e;
    }
  }

  protected abstract void startPreloader(Stage stage) throws Exception;

  public Stage getStage() {
    return stage;
  }
}
