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
package de.ks.integration;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityController;
import de.ks.application.Navigator;
import de.ks.idnadrev.IdnadrevWindow;
import de.ks.idnadrev.entity.Thought;
import de.ks.idnadrev.thought.collect.AddThought;
import de.ks.idnadrev.thought.collect.ThoughtActivity;
import de.ks.launch.JavaFXService;
import de.ks.launch.Launcher;
import de.ks.reflection.PropertyPath;
import de.ks.util.FXPlatform;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.loadui.testfx.GuiTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.loadui.testfx.Assertions.verifyThat;
import static org.loadui.testfx.controls.impl.VisibleNodesMatcher.visible;

@RunWith(LauncherRunner.class)
public class SimpleWorkflowTest extends GuiTest {
  private static final Logger log = LoggerFactory.getLogger(SimpleWorkflowTest.class);
  private static final String name = PropertyPath.property(Thought.class, t -> t.getName());
  private Parent rootNode;
  private IdnadrevWindow mainWindow;

  @Before
  public void setupGuiTest() throws Exception {
    primaryStage = Launcher.instance.getService(JavaFXService.class).getStage();
    FXPlatform.invokeLater(() -> primaryStage.setScene(null));
    setupStages(this);
  }

  @Override
  protected Parent getRootNode() {
    if (rootNode == null) {
      mainWindow = CDI.current().select(IdnadrevWindow.class).get();
      BorderPane node = (BorderPane) mainWindow.getNode();
      Navigator.registerWithExistingPane(primaryStage, node);
      rootNode = node;
    }
    return rootNode;
  }

  @Test
  public void testCompleteSimpleWorkflow() throws Exception {
    ActivityController activityController = CDI.current().select(ActivityController.class).get();
    assertEquals(ThoughtActivity.class, activityController.getCurrentActivity().getClass());

    AddThought addThought = activityController.getCurrentController();


    waitUntil("#name", is(visible()));
    clickOn("#name");
    verifyThat("#save", (Button s) -> s.isDisabled());

    type("Hello Sauerland");
    verifyThat("#save", (Button s) -> !s.isDisabled());

  }
}
