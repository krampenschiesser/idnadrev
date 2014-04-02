/*
 * Copyright [${YEAR}] [Christian Loehnert]
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

package de.ks.activity;


import de.ks.activity.callback.InitializeTaskLinks;
import de.ks.activity.callback.InitializeViewLinks;
import de.ks.application.Navigator;
import de.ks.application.PresentationArea;
import de.ks.application.fxml.DefaultLoader;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.junit.Test;

import javax.enterprise.inject.spi.CDI;

import static de.ks.JunitMatchers.withRetry;
import static org.junit.Assert.*;

public class ActivityTest extends AbstractActivityTest {
  @Test
  public void testViewLinkNavigation() throws Exception {
    activityController.start(activity);
    PresentationArea mainArea = navigator.getMainArea();
    Node currentNode = mainArea.getCurrentNode();
    assertNotNull(currentNode);
    Button detailButton = (Button) currentNode.lookup("#showDetails");
    assertNotNull(detailButton);
    Button switchViewButton = (Button) currentNode.lookup("#switchView");
    assertNotNull(switchViewButton);

    assertNull(navigator.getPresentationArea(Navigator.RIGHT_AREA).getCurrentNode());

    assertNotNull(detailButton.getOnAction());
    detailButton.getOnAction().handle(new ActionEvent());
    assertNotNull(navigator.getPresentationArea(Navigator.RIGHT_AREA).getCurrentNode());

    assertNotNull(switchViewButton.getOnAction());
    switchViewButton.getOnAction().handle(new ActionEvent());
    Node nextNode = navigator.getMainArea().getCurrentNode();
    assertNotNull(nextNode);

    assertNotSame(currentNode, nextNode);
    Button back = (Button) nextNode.lookup("#back");
    assertNotNull(back);

    assertNotNull(back.getOnAction());
    back.getOnAction().handle(new ActionEvent());

    assertSame(currentNode, navigator.getMainArea().getCurrentNode());
  }

  @Test
  public void testInitializeViewLinks() throws Exception {
    DefaultLoader<StackPane, ActivityHome> loader = new DefaultLoader<>(ActivityHome.class);
    InitializeViewLinks viewLinks = new InitializeViewLinks(activity.getViewLinks(), activityController);
    viewLinks.accept(loader.getController(), loader.getView());

    Button button = (Button) loader.getView().lookup("#showDetails");
    assertNotNull(button.getOnAction());

    button = (Button) loader.getView().lookup("#switchView");
    assertNotNull(button.getOnAction());
  }

  @Test
  public void testInitializeTaskLinks() throws Exception {
    activityController.start(activity);
    DefaultLoader<StackPane, DetailController> loader = new DefaultLoader<>(DetailController.class);
    InitializeTaskLinks taskLinks = new InitializeTaskLinks(activity.getTaskLinks(), activityController);
    taskLinks.accept(loader.getController(), loader.getView());

    Button button = (Button) loader.getView().lookup("#pressMe");
    assertNotNull(button.getOnAction());
    button.getOnAction().handle(new ActionEvent());

    assertTrue(withRetry(() -> CDI.current().select(ActivityAction.class).get().isExecuted()));
  }
}
