/*
 * Copyright [2016] [Christian Loehnert]
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

package de.ks.idnadrev;

import de.ks.standbein.application.ApplicationRoot;
import de.ks.standbein.application.MainWindow;
import de.ks.standbein.application.fxml.DefaultLoader;
import de.ks.standbein.javafx.NodeLookup;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class IdnadrevWindow extends MainWindow {
  private static final Logger log = LoggerFactory.getLogger(IdnadrevWindow.class);

  //  @Inject
//  protected Provider<WorkingOnTaskLink> workingOnTaskLinkProvider;
  @Inject
  private DefaultLoader<BorderPane, Object> rootLoader;

  BorderPane borderPane;
  StackPane contentPane;
  HBox progressBox;
//  private WorkingOnTaskLink workingOnTaskLink;

  @Override
  public ApplicationRoot getRoot() {
    rootLoader.load(getClass().getResource(getClass().getSimpleName() + ".fxml"));
    borderPane = rootLoader.getView();

//    workingOnTaskLink = workingOnTaskLinkProvider.get();

    StackPane topPane = (StackPane) borderPane.getTop();
    progressBox = (HBox) topPane.getChildren().get(0);
//    progressBox.getChildren().add(workingOnTaskLink.getHyperlink());

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

//  public WorkingOnTaskLink getWorkingOnTaskLink() {
//    return workingOnTaskLink;
//  }
}
