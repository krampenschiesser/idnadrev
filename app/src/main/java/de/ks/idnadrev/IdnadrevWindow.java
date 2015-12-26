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

import de.ks.idnadrev.task.work.WorkingOnTaskLink;
import de.ks.standbein.activity.ActivityController;
import de.ks.standbein.application.ApplicationRoot;
import de.ks.standbein.application.MainWindow;
import de.ks.standbein.application.fxml.DefaultLoader;
import de.ks.standbein.javafx.NodeLookup;
import de.ks.standbein.menu.MenuBarCreator;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.MenuBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class IdnadrevWindow extends MainWindow {
  private static final Logger log = LoggerFactory.getLogger(IdnadrevWindow.class);

  @Inject
  ActivityController activityController;
  @Inject
  MenuBarCreator menuBarCreator;

  @Inject
  protected Provider<WorkingOnTaskLink> workingOnTaskLinkProvider;
  @Inject
  private DefaultLoader<BorderPane, Object> rootLoader;
  @Inject
  private DefaultLoader<GridPane, ButtonBar> buttonBarLoader;

  private BorderPane borderPane;
  private ButtonBar buttonBar;
  private GridPane buttonBarView;
  private StackPane contentPane;
  protected HBox progressBox;
  private WorkingOnTaskLink workingOnTaskLink;

  @Override
  public ApplicationRoot getRoot() {
    rootLoader.load(getClass().getResource(getClass().getSimpleName() + ".fxml"));
    borderPane = rootLoader.getView();
    VBox vBox = new VBox();
    vBox.setMinSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
    vBox.setMaxSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
    MenuBar menu = menuBarCreator.createMenu("/main");
    menu.setMinSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
    vBox.getChildren().add(menu);

    workingOnTaskLink = workingOnTaskLinkProvider.get();

    StackPane topPane = (StackPane) borderPane.getTop();
    progressBox = (HBox) topPane.getChildren().get(0);
    progressBox.getChildren().add(workingOnTaskLink.getHyperlink());
    topPane.getChildren().add(0, menu);

    buttonBarLoader.load(ButtonBar.class);
    buttonBar = buttonBarLoader.getController();
    buttonBarView = buttonBarLoader.getView();

    contentPane = new StackPane();
    borderPane.setCenter(contentPane);

    borderPane.setOnKeyReleased(this::checkShortcut);

    ApplicationRoot applicationRoot = new ApplicationRoot(borderPane, contentPane);
    return applicationRoot;
  }

  @Override
  public Parent getNode() {
    return new StackPane();
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
      buttonBar.createTask();
      event.consume();
    } else if (code == KeyCode.F5) {
      buttonBar.createTextInfo();
      event.consume();
    } else if (code == KeyCode.F6) {
      buttonBar.viewThoughts();
      event.consume();
    } else if (code == KeyCode.F7) {
      buttonBar.viewTasks();
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
    } else if (code == KeyCode.F11) {
      buttonBar.chooseNextTask();
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

  public HBox getProgressBox() {
    return progressBox;
  }

  public WorkingOnTaskLink getWorkingOnTaskLink() {
    return workingOnTaskLink;
  }
}
