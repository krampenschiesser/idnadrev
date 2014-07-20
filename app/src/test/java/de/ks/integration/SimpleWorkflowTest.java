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
import de.ks.idnadrev.entity.FileReference;
import de.ks.idnadrev.entity.Thought;
import de.ks.idnadrev.task.create.CreateTaskActivity;
import de.ks.idnadrev.task.create.MainTaskInfo;
import de.ks.idnadrev.thought.collect.AddThought;
import de.ks.idnadrev.thought.collect.ThoughtActivity;
import de.ks.idnadrev.thought.view.ViewThoughts;
import de.ks.idnadrev.thought.view.ViewThoughtsActivity;
import de.ks.launch.JavaFXService;
import de.ks.launch.Launcher;
import de.ks.persistence.PersistentWork;
import de.ks.reflection.PropertyPath;
import de.ks.util.FXPlatform;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.loadui.testfx.GuiTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.enterprise.inject.spi.CDI;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.loadui.testfx.Assertions.verifyThat;
import static org.loadui.testfx.controls.impl.EnabledMatcher.disabled;
import static org.loadui.testfx.controls.impl.EnabledMatcher.enabled;
import static org.loadui.testfx.controls.impl.VisibleNodesMatcher.visible;

@RunWith(LauncherRunner.class)
public class SimpleWorkflowTest extends GuiTest {
  private static final Logger log = LoggerFactory.getLogger(SimpleWorkflowTest.class);
  private static final String name = PropertyPath.property(Thought.class, t -> t.getName());
  private Parent rootNode;
  private IdnadrevWindow mainWindow;

  @Before
  public void setupGuiTest() throws Exception {
    PersistentWork.deleteAllOf(FileReference.class, Thought.class);

    primaryStage = Launcher.instance.getService(JavaFXService.class).getStage();
    FXPlatform.invokeLater(() -> primaryStage.setScene(null));
    setupStages(this);
  }

  @After
  public void tearDown() throws Exception {
    FXPlatform.waitForFX();
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

  @Ignore
  @Test
  public void testCompleteSimpleWorkflow() throws Exception {
    String name = "Hello Sauerland";
    String description = "= title\n\n== content\n\nhello world";

    ActivityController activityController = CDI.current().select(ActivityController.class).get();
    activityController.waitForTasks();
    assertEquals(ThoughtActivity.class, activityController.getCurrentActivity().getClass());

    AddThought addThought = activityController.getCurrentController();

    waitUntil(addThought.getName(), is(visible()));
    clickOn(addThought.getName());
    verifyThat(addThought.getSave(), is(disabled()));

    type(name);
    waitUntil(addThought.getSave(), is(enabled()));
    verifyThat(addThought.getSave(), is(enabled()));

    clickOn(addThought.getDescription().getEditor());
    type(description);

    FXPlatform.invokeLater(() -> addThought.getDescription().selectPreview());

    waitUntil(addThought.getDescription().getPreview(), is(visible()));
    waitUntil(addThought.getDescription().getPreview(), preview -> preview.getEngine().getDocument() != null, 10);
    verifyThat(addThought.getDescription().getPreview(), preview -> preview.getEngine().getDocument() != null);
    verifyThat(addThought.getDescription().getPreview(), preview -> toHtml(preview.getEngine().getDocument()).contains("html"));
    verifyThat(addThought.getDescription().getPreview(), preview -> toHtml(preview.getEngine().getDocument()).contains("hello world"));

    clickOn(addThought.getSave());
    waitUntil(addThought.getSave(), is(disabled()));

    verifyThat(addThought.getSave(), is(disabled()));
    verifyThat(addThought.getName().getText(), isEmptyOrNullString());
    verifyThat(addThought.getDescription().getText(), isEmptyOrNullString());

    //select view thoughts
    type(KeyCode.F2);
    activityController.waitForTasks();
    waitUntil(activityController.getCurrentActivity(), instanceOf(ViewThoughtsActivity.class));

    ViewThoughts viewThoughts = activityController.getControllerInstance(ViewThoughts.class);
    waitUntil(viewThoughts.getTable(), is(visible()));

    verifyThat(viewThoughts.getTable().getItems(), contains(new Thought(name).setDescription(description)));
    verifyThat(viewThoughts.getTable().getItems().size(), equalTo(1));

    FXPlatform.invokeLater(() -> viewThoughts.getTable().getSelectionModel().select(0));

    clickOn(viewThoughts.getToTask());
    clickOn(viewThoughts.getToTask());

    activityController.waitForTasks();
    waitUntil(activityController.getCurrentActivity(), instanceOf(CreateTaskActivity.class));

    MainTaskInfo createTask = activityController.getControllerInstance(MainTaskInfo.class);
    waitUntil(createTask.getName(), is(visible()));
    verifyThat(createTask.getName().getText(), containsString(name));
    verifyThat(createTask.getDescription().getEditor().getText(), containsString(description));
  }

  protected String toHtml(Document doc) {
    try {
      DOMSource domSource = new DOMSource(doc);
      StringWriter writer = new StringWriter();
      StreamResult result = new StreamResult(writer);
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.transform(domSource, result);
      return writer.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
