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

package de.ks.beagle;


import de.ks.application.MainWindow;
import de.ks.application.fxml.DefaultLoader;
import de.ks.menu.presenter.MenuBarPresenter;
import de.ks.menu.sink.ContentSink;
import de.ks.menu.sink.PopupSink;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 *
 */
public class BeagleWindow extends MainWindow {
  private static final Logger log = LoggerFactory.getLogger(BeagleWindow.class);

  @Inject
  MenuBarPresenter menuBarPresenter;
  @Inject
  PopupSink popupSink;
  @Inject
  ContentSink contentSink;

  private BorderPane borderPane;
  private DefaultLoader<BorderPane, Object> loader;

  @PostConstruct
  public void initialize() {
    popupSink.setMenuPath("/main/help");
    contentSink.setMenuPath("/main");
    loader = new DefaultLoader<>(BeagleWindow.class);
  }

  @Override
  public Parent getNode() {
    if (borderPane == null) {
      borderPane = loader.getView();
      borderPane.setTop(menuBarPresenter.getMenu("/main"));

      StackPane contentPane = new StackPane();
      borderPane.setCenter(contentPane);
      contentSink.setPane(contentPane);
    }
    return borderPane;
  }

  @Override
  public String getApplicationTitle() {
    return "Beagle Version 0.3";
  }
}
