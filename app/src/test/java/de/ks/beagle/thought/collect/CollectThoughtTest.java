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
package de.ks.beagle.thought.collect;

import de.ks.FXPlatform;
import de.ks.LauncherRunner;
import de.ks.activity.ActivityController;
import de.ks.beagle.entity.Thought;
import de.ks.launch.JavaFXService;
import de.ks.launch.Launcher;
import de.ks.persistence.PersistentWork;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class CollectThoughtTest {
  private static final Logger log = LoggerFactory.getLogger(CollectThoughtTest.class);
  private Scene scene;
  @Inject
  ActivityController controller;

  private AddThought addThought;

  @Before
  public void setUp() throws Exception {
    JavaFXService service = Launcher.instance.getService(JavaFXService.class);
    Stage stage = service.getStage();
    scene = stage.getScene();
    ThoughtActivity activity = controller.start(ThoughtActivity.class);
    controller.waitForDataSourceLoading();

    addThought = activity.getCurrentController();
  }


  @After
  public void tearDown() throws Exception {
    controller.stop(ThoughtActivity.class);
  }

  @Test
  public void testClipboard2LineString() throws Exception {
    String clipboardText = "title\ndescription";
    copy2Clipboard(clipboardText);

    assertEquals(clipboardText, addThought.description.getText());
    assertEquals("title", addThought.name.getText());
    assertTrue(addThought.name.isFocused());
  }

  @Test
  public void testClipboardSingleLineString() throws Exception {
    String clipboardText = "description";
    copy2Clipboard(clipboardText);

    assertEquals(clipboardText, addThought.description.getText());
    assertNull(addThought.name.getText());
    assertTrue(addThought.name.isFocused());
  }

  private void copy2Clipboard(String clipboardText) {
    FXPlatform.invokeLater(() -> {
      Clipboard clipboard = Clipboard.getSystemClipboard();
      HashMap<DataFormat, Object> content = new HashMap<>();
      content.put(DataFormat.PLAIN_TEXT, clipboardText);
      clipboard.setContent(content);
      addThought.processClipboard(clipboard);
    });
  }

  @Test
  public void testSaveThought() throws Exception {
    FXPlatform.invokeLater(() -> {
      addThought.name.setText("name");
      addThought.description.setText("description");

      addThought.save.getOnAction().handle(new ActionEvent());
    });

    List<Thought> thoughts = PersistentWork.from(Thought.class);
    assertEquals(1, thoughts.size());

    Thought thought = thoughts.get(0);
    assertEquals("name", thought.getName());
    assertEquals("description", thought.getDescription());

    assertNull(addThought.description.getText());
    assertNull(addThought.name.getText());
  }
}
