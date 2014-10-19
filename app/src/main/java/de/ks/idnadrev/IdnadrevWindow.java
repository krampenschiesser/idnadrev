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

import de.ks.activity.ActivityCfg;
import de.ks.activity.ActivityController;
import de.ks.activity.ActivityHint;
import de.ks.application.MainWindow;
import de.ks.application.fxml.DefaultLoader;
import de.ks.idnadrev.overview.OverviewActivity;
import de.ks.javafx.NodeLookup;
import de.ks.menu.presenter.MenuBarPresenter;
import de.ks.menu.sink.ContentSink;
import de.ks.menu.sink.PopupSink;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.MenuBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
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
  public static final String PROPERTY_INITIAL_ACTIVITY = "initialActivtiy";
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
  private GridPane buttonBarView;
  private StackPane contentPane;

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
      MenuBar menu = menuBarPresenter.getMenu("/main");
      menu.setMinSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
      vBox.getChildren().add(menu);
      borderPane.setTop(menu);

      DefaultLoader<GridPane, ButtonBar> loader = new DefaultLoader<>(ButtonBar.class);
      buttonBar = loader.getController();
      buttonBarView = loader.getView();
//      borderPane.setRight(loader.getView());

      contentPane = new StackPane();
      borderPane.setCenter(contentPane);
      contentSink.setPane(contentPane);

      borderPane.setOnKeyReleased(this::checkShortcut);

      Platform.runLater(this::startInitialActivity);
    }
    return borderPane;
  }

  protected void startInitialActivity() {
    String activityClass = System.getProperty(PROPERTY_INITIAL_ACTIVITY, OverviewActivity.class.getName());
    if (!activityClass.isEmpty()) {
      try {
        @SuppressWarnings("unchecked") Class<? extends ActivityCfg> clazz = (Class<? extends ActivityCfg>) Class.forName(activityClass);
        activityController.startOrResume(new ActivityHint(clazz));
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          //
        }
      } catch (ClassNotFoundException e) {
        log.error("Could not load activity class {}", activityClass, e);
      }
    }
  }

  private void checkShortcut(KeyEvent event) {
    KeyCode code = event.getCode();

    if (code == KeyCode.F1) {
      if (buttonBar.isShowing()) {
        buttonBar.hide();
      } else {
        buttonBar.show(borderPane);
      }
      event.consume();
    } else if (code == KeyCode.F2) {
      buttonBar.overview();
      event.consume();
    } else if (code == KeyCode.F3) {
      buttonBar.addThought();
      event.consume();
    } else if (code == KeyCode.F4) {
      buttonBar.createTextInfo();
      event.consume();
    } else if (code == KeyCode.F5) {
      buttonBar.viewThoughts();
      event.consume();
    } else if (code == KeyCode.F6) {
      buttonBar.viewTasks();
      event.consume();
    } else if (code == KeyCode.F7) {
      buttonBar.chooseNextTask();
      event.consume();
    } else if (code == KeyCode.F8) {
      buttonBar.informationOverview();
      event.consume();
    } else if (code == KeyCode.F9) {
      buttonBar.planWeek();
      event.consume();
    } else if (code == KeyCode.F10) {
      buttonBar.weeklyDone();
      event.consume();
    } else if (code == KeyCode.F12) {
      buttonBar.fastTrack();
      event.consume();
    }

    if (event.isControlDown() && event.getCode() == KeyCode.ENTER) {
      Set<Node> defaultButtons = NodeLookup.getAllNodes(borderPane, n -> n.isVisible() && n instanceof Button && ((Button) n).isDefaultButton());
      if (!defaultButtons.isEmpty()) {
        Button defaultButton = (Button) defaultButtons.iterator().next();
        if (!defaultButton.isDisabled()) {
          log.debug("Executing default button {} on ctrl+enter", defaultButton);
          defaultButton.getOnAction().handle(null);
        }
        event.consume();
      }
    }
  }

  @Override
  public String getApplicationTitle() {
    return "Idnadrev Version " + Application.versioning.getVersionInfo().getVersionString();
  }
}
