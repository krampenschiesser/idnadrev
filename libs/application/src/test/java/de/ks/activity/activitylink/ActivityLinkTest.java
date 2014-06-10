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
package de.ks.activity.activitylink;

import de.ks.FXPlatform;
import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.activity.ActivityController;
import de.ks.activity.context.ActivityContext;
import de.ks.activity.context.ActivityStore;
import de.ks.application.Navigator;
import de.ks.launch.JavaFXService;
import de.ks.launch.Launcher;
import javafx.application.Platform;
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
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class ActivityLinkTest {
  public static final String STANDARD_HINT = "withStandardHint";
  public static final String RETURN_HINT = "withReturnHint";
  @Inject
  ActivityController controller;
  @Inject
  ActivityStore store;
  private ActivityCfg activityCfgA;

  @Before
  public void setUp() throws Exception {
    Navigator.registerWithBorderPane(Launcher.instance.getService(JavaFXService.class).getStage());

    activityCfgA = new ActivityCfg(ActivityADS.class, ActivityAController.class);

    activityCfgA.withActivity(ActivityAController.class, STANDARD_HINT, ActivityB.class, (ActivityAModel m) -> new ActivityBModel(m.getId()));
    activityCfgA.withActivityAndReturn(ActivityAController.class, RETURN_HINT, ActivityB.class, (ActivityAModel m) -> new ActivityBModel(m.getId() + "Hello"), (ActivityBModel m2) -> new ActivityAModel(m2.getDescription() + " Sauerland"));
  }

  @After
  public void tearDown() throws Exception {
    controller.stop(activityCfgA);
    controller.stop(ActivityB.class);
    ActivityContext.stopAll();
  }

  @Test
  public void testStandardHint() throws Exception {
    controller.start(activityCfgA);
    controller.waitForDataSource();

    ActivityAModel model = store.getModel();
    model.setId("bla");
    store.getBinding().fireValueChangedEvent();
    FXPlatform.waitForFX();

    Node currentNode = controller.getCurrentNode();
    Label idLabel = (Label) currentNode.lookup("#id");
    assertEquals("bla", idLabel.getText());

    Button standardHintButton = (Button) currentNode.lookup("#" + STANDARD_HINT);
    assertNotNull(standardHintButton);

    EventHandler<ActionEvent> onAction = standardHintButton.getOnAction();
    assertNotNull(onAction);
    onAction.handle(new ActionEvent());
    Node activityBNode = Navigator.getCurrentNavigator().getMainArea().getCurrentNode();
    controller.waitForDataSource();
    TextField descriptionInput = (TextField) activityBNode.lookup("#description");
    assertEquals("bla", descriptionInput.getText());
  }

  @Test
  public void testReturnHint() throws Exception {
    controller.start(activityCfgA);
    controller.waitForDataSource();

    ActivityAModel model = store.getModel();
    model.setId("bla");
    store.getBinding().fireValueChangedEvent();
    FXPlatform.waitForFX();

    Node activityANode = controller.getCurrentNode();
    Label idLabel = (Label) activityANode.lookup("#id");
    assertEquals("bla", idLabel.getText());

    Button returnHintButton = (Button) activityANode.lookup("#" + RETURN_HINT);
    assertNotNull(returnHintButton);

    EventHandler<ActionEvent> onAction = returnHintButton.getOnAction();
    assertNotNull(onAction);
    onAction.handle(new ActionEvent());
    Node activityBNode = Navigator.getCurrentNavigator().getMainArea().getCurrentNode();
    controller.waitForDataSource();
    TextField descriptionInput = (TextField) activityBNode.lookup("#description");
    assertEquals("bla" + "Hello", descriptionInput.getText());

    Button finishButton = (Button) activityBNode.lookup("#finish");
    EventHandler<ActionEvent> finishAction = finishButton.getOnAction();
    assertNotNull(finishAction);

    finishAction.handle(new ActionEvent());
    Thread.sleep(300);
    waitForJavaFXThread();
    controller.waitForDataSource();

    Node topNode = Navigator.getCurrentNavigator().getMainArea().getCurrentNode();
    assertNotEquals(activityBNode, topNode);
    assertEquals(activityANode, topNode);
  }

  public void waitForJavaFXThread() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(() -> latch.countDown());
    latch.await();
  }
}
