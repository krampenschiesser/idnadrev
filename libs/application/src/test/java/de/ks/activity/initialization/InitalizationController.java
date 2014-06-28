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
package de.ks.activity.initialization;

import de.ks.application.fxml.DefaultLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class InitalizationController implements Initializable {
  @Inject
  ActivityInitialization initialization;
  boolean didLoadOtherController = false;

  OtherController other;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    CompletableFuture<DefaultLoader<Node, OtherController>> callback = initialization.loadAdditionalController(OtherController.class);
    callback.thenAccept(loader -> other = (OtherController) loader.getController());
    callback.thenRun(() -> didLoadOtherController = true);
  }
}
