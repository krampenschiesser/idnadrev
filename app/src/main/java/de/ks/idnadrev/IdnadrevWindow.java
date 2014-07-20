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

import de.ks.activity.ActivityController;
import de.ks.application.MainWindow;
import de.ks.application.fxml.DefaultLoader;
import de.ks.idnadrev.thought.add.AddThoughtActivity;
import de.ks.javafx.NodeLookup;
import de.ks.menu.presenter.MenuBarPresenter;
import de.ks.menu.sink.ContentSink;
import de.ks.menu.sink.PopupSink;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Set;

/**
 *
 */
public class IdnadrevWindow extends MainWindow {
  private static final Logger log = LoggerFactory.getLogger(IdnadrevWindow.class);

  @Inject
  MenuBarPresenter menuBarPresenter;
  @Inject
  PopupSink popupSink;
  @Inject
  ContentSink contentSink;
  @Inject
  ActivityController activityController;

  private BorderPane borderPane;
  private DefaultLoader<BorderPane, Object> loader;
  private ButtonBar buttonBar;

  @PostConstruct
  public void initialize() {
    popupSink.setMenuPath("/main/help");
    contentSink.setMenuPath("/main");
    loader = new DefaultLoader<>(IdnadrevWindow.class);
  }

  @Override
  public Parent getNode() {
    if (borderPane == null) {
      borderPane = loader.getView();
      VBox vBox = new VBox();
      vBox.setMinSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
      vBox.setMaxSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
      vBox.getChildren().add(menuBarPresenter.getMenu("/main"));
      borderPane.setTop(vBox);

      DefaultLoader<VBox, ButtonBar> loader = new DefaultLoader<>(ButtonBar.class);
      buttonBar = loader.getController();
      borderPane.setRight(loader.getView());

      StackPane contentPane = new StackPane();
      borderPane.setCenter(contentPane);
      contentSink.setPane(contentPane);

      borderPane.setOnKeyReleased(this::checkShortcut);

      activityController.start(AddThoughtActivity.class);
    }
    return borderPane;
  }

  private void checkShortcut(KeyEvent event) {
    KeyCode code = event.getCode();

    if (code == KeyCode.F1) {
      buttonBar.addThought();
      event.consume();
    } else if (code == KeyCode.F2) {
      buttonBar.viewThoughts();
      event.consume();
    } else if (code == KeyCode.F3) {
      buttonBar.createTask();
      event.consume();
    } else if (code == KeyCode.F4) {
      buttonBar.viewTasks();
      event.consume();
    }

    if (event.isControlDown() && event.getCode() == KeyCode.S) {
      Set<Node> defaultButtons = NodeLookup.getAllNodes(borderPane, n -> n.isVisible() && n instanceof Button && ((Button) n).isDefaultButton());
      if (defaultButtons.size() == 1) {
        Button defaultButton = (Button) defaultButtons.iterator().next();
        log.debug("Executing default button {} on ctrl+s", defaultButton);
        defaultButton.getOnAction().handle(null);
        event.consume();
      } else {
        log.warn("More than one default button found");
      }
    }
  }

  @Override
  public String getApplicationTitle() {
    return "Idnadrev Version 0.3";
  }
}
