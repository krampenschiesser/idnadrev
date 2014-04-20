/**
 * Copyright [2014] [Christian Loehnert]
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
package de.ks.activity.activitylink;

import de.ks.JFXCDIRunner;
import de.ks.activity.Activity;
import de.ks.activity.ActivityController;
import de.ks.activity.context.ActivityContext;
import de.ks.activity.context.ActivityStore;
import de.ks.application.Navigator;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JFXCDIRunner.class)
public class ActivityLinkTest {
  public static final String STANDARD_HINT = "withStandardHint";
  public static final String RETURN_HINT = "withReturnHint";
  @Inject
  ActivityController controller;
  @Inject
  ActivityStore store;
  private Activity activityA;

  @Before
  public void setUp() throws Exception {
    Navigator.registerWithBorderPane(JFXCDIRunner.getStage());

    activityA = new Activity(ActivityADS.class, ActivityAController.class);

    activityA.withActivity(ActivityAController.class, STANDARD_HINT, ActivityB.class, (ActivityAModel m) -> new ActivityBModel(m.getId()));
    activityA.withActivityAndReturn(ActivityAController.class, RETURN_HINT, ActivityB.class, (ActivityAModel m) -> new ActivityBModel(m.getId() + "Hello"), (ActivityBModel m2) -> new ActivityAModel(m2.getDescription() + " Sauerland"));
  }

  @After
  public void tearDown() throws Exception {
    controller.stop(activityA);
    controller.stop(ActivityB.class.getName());
    ActivityContext.stopAll();
  }

  @Test
  public void testStandardHint() throws Exception {
    controller.start(activityA);
    controller.waitForDataSourceLoading();

    ActivityAModel model = store.getModel();
    model.setId("bla");
    store.getBinding().fireValueChangedEvent();

    Node currentNode = activityA.getCurrentNode();
    Label idLabel = (Label) currentNode.lookup("#id");
    assertEquals("bla", idLabel.getText());

    Button standardHintButton = (Button) currentNode.lookup("#" + STANDARD_HINT);
    assertNotNull(standardHintButton);

    EventHandler<ActionEvent> onAction = standardHintButton.getOnAction();
    assertNotNull(onAction);
    onAction.handle(new ActionEvent());
    Node activityBNode = Navigator.getCurrentNavigator().getMainArea().getCurrentNode();
    controller.waitForDataSourceLoading();
    TextField descriptionInput = (TextField) activityBNode.lookup("#description");
    assertEquals("bla" + "Hello", descriptionInput.getText());
  }

  @Test
  public void testReturnHint() throws Exception {
    controller.start(activityA);
    controller.waitForDataSourceLoading();

    ActivityAModel model = store.getModel();
    model.setId("bla");
    store.getBinding().fireValueChangedEvent();

    Node activityANode = activityA.getCurrentNode();
    Label idLabel = (Label) activityANode.lookup("#id");
    assertEquals("bla", idLabel.getText());

    Button returnHintButton = (Button) activityANode.lookup("#" + RETURN_HINT);
    assertNotNull(returnHintButton);

    EventHandler<ActionEvent> onAction = returnHintButton.getOnAction();
    assertNotNull(onAction);
    onAction.handle(new ActionEvent());
    Node activityBNode = Navigator.getCurrentNavigator().getMainArea().getCurrentNode();
    controller.waitForDataSourceLoading();
    TextField descriptionInput = (TextField) activityBNode.lookup("#description");
    assertEquals("bla" + "Hello", descriptionInput.getText());

    Button finishButton = (Button) activityBNode.lookup("#finish");
    EventHandler<ActionEvent> finishAction = finishButton.getOnAction();
    assertNotNull(finishAction);

    finishAction.handle(new ActionEvent());
    controller.waitForDataSourceLoading();

    assertEquals(activityANode, Navigator.getCurrentNavigator().getMainArea().getCurrentNode());
  }
}
