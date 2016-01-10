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
package de.ks.idnadrev.thought.add;

import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.IdnadrevIntegrationTestModule;
import de.ks.idnadrev.entity.Thought;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.TempFileRule;
import de.ks.standbein.activity.ActivityCfg;
import de.ks.standbein.datasource.DataSource;
import de.ks.util.FXPlatform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class AddThoughtTest extends ActivityTest {
  private static final Logger log = LoggerFactory.getLogger(AddThoughtTest.class);
  @Rule
  public TempFileRule testFiles = new TempFileRule(2);

  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IdnadrevIntegrationTestModule()).launchServices();

  private Scene scene;

  private AddThought addThought;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return AddThoughtActivity.class;
  }

  @Before
  public void setUp() throws Exception {
    addThought = activityController.getCurrentController();
    FXPlatform.invokeLater(() -> {
      Clipboard.getSystemClipboard().clear();
    });
  }

  @Test
  public void testClipboard2LineString() throws Exception {
    String clipboardText = "title\nclipboard";
    copy2Clipboard(clipboardText);

    FXPlatform.waitForFX();
    assertEquals(clipboardText, addThought.description.getText());
    assertEquals("title", addThought.name.getText());

    FXPlatform.invokeLater(() -> {
      Clipboard.getSystemClipboard().clear();
      addThought.name.setText(null);
      addThought.description.setText(null);
    });
  }

  @Test
  public void testClipboardSingleLineString() throws Exception {
    clearClipBoard();
    FXPlatform.waitForFX();
    FXPlatform.invokeLater(() -> {
      addThought.name.setText("");
      addThought.description.setText("");
    });
    clearClipBoard();
    String clipboardText = "singleClip";
    copy2Clipboard(clipboardText);
    FXPlatform.waitForFX();
    assertEquals(clipboardText, addThought.description.getText());
    assertEquals(clipboardText, addThought.name.getText());
  }

  protected void clearClipBoard() {
    FXPlatform.invokeLater(() -> {
      Clipboard clipboard = Clipboard.getSystemClipboard();
      clipboard.clear();
    });
  }

  private void copy2Clipboard(String clipboardText) {
    FXPlatform.invokeLater(() -> {
      Clipboard clipboard = Clipboard.getSystemClipboard();
      clipboard.clear();
      clipboard.getFiles().clear();

      HashMap<DataFormat, Object> content = new HashMap<>();
      content.put(DataFormat.PLAIN_TEXT, clipboardText);
      clipboard.setContent(content);
      addThought.processClipboard(clipboard);
    });
  }

  @Test
  public void testSaveThought() throws Exception {
    FXPlatform.invokeLater(() -> {
      Clipboard.getSystemClipboard().clear();
      addThought.name.setText("name");
      addThought.description.setText("desc");
    });
    Thread.sleep(50);
    activityController.waitForTasks();
    FXPlatform.invokeLater(() -> {
      addThought.save.getOnAction().handle(new ActionEvent());
    });
    activityController.waitForTasks();

    List<Thought> thoughts = persistentWork.from(Thought.class);
    assertEquals(1, thoughts.size());

    Thought thought = thoughts.get(0);
    assertEquals("name", thought.getName());
    assertEquals("desc", thought.getDescription());
  }

  @Test
  public void testEditThought() throws Exception {
    copy2Clipboard("hello world");
    Thought thought = new Thought("testThought");
    persistentWork.persist(thought.setDescription("hello Sauerland!"));


    DataSource datasource = store.getDatasource();
    datasource.setLoadingHint(thought);
    activityController.reload();
    activityController.waitForDataSource();

    assertEquals("hello Sauerland!", addThought.description.getText());
  }

  @Test
  public void testFilesInClipBoard() throws Exception {
    HashMap<DataFormat, Object> content = new HashMap<>();
    content.put(DataFormat.FILES, testFiles.getFiles());
    StringBuilder plainText = testFiles.getFiles().stream().collect(StringBuilder::new, //
      (s, file) -> s.append(file.getAbsolutePath()).append("\n"),//
      (s, s2) -> s.append(s2));
    content.put(DataFormat.PLAIN_TEXT, plainText.toString());

    FXPlatform.invokeLater(() -> {
      Clipboard clipboard = Clipboard.getSystemClipboard();
      clipboard.clear();
      clipboard.setContent(content);

      addThought.processClipboard(clipboard);
    });

    assertFalse(addThought.name.textProperty().isEmpty().get());
    assertFalse(addThought.description.getText().isEmpty());
    assertEquals(2, addThought.fileViewController.getFiles().size());

    FXPlatform.invokeLater(() -> {
      addThought.save.getOnAction().handle(new ActionEvent());
    });
    Thread.sleep(100);
    activityController.waitForTasks();
    activityController.getExecutorService().waitForAllTasksDone();

    List<Thought> thoughts = persistentWork.from(Thought.class);
    assertEquals(1, thoughts.size());

    Thought thought = thoughts.get(0);
    assertEquals(2, thought.getFiles().size());
  }

  @Test
  public void testFilesInClipBoardWithRemove() throws Exception {
    HashMap<DataFormat, Object> content = new HashMap<>();
    content.put(DataFormat.FILES, testFiles.getFiles());
    StringBuilder plainText = testFiles.getFiles().stream().collect(StringBuilder::new, //
      (s, file) -> s.append(file.getAbsolutePath()).append("\n"),//
      (s, s2) -> s.append(s2));
    content.put(DataFormat.PLAIN_TEXT, plainText.toString());

    FXPlatform.invokeLater(() -> {
      Clipboard clipboard = Clipboard.getSystemClipboard();
      clipboard.clear();
      clipboard.setContent(content);

      addThought.processClipboard(clipboard);
    });
    FXPlatform.waitForFX();
    activityController.waitForTasks();

    assertFalse(addThought.name.textProperty().isEmpty().get());
    assertFalse(addThought.description.getText().isEmpty());
    assertEquals(2, addThought.fileViewController.getFiles().size());


    FXPlatform.invokeLater(() -> {
      MultipleSelectionModel<File> selectionModel = addThought.fileViewController.getFileList().getSelectionModel();
      selectionModel.clearSelection();
      selectionModel.select(0);
      addThought.fileViewController.removeFile();
      addThought.save.getOnAction().handle(new ActionEvent());
    });
    FXPlatform.waitForFX();
    activityController.waitForDataSource();
    activityController.waitForTasks();

    List<Thought> thoughts = persistentWork.from(Thought.class);
    assertEquals(1, thoughts.size());

    Thought thought = thoughts.get(0);
    assertEquals(1, thought.getFiles().size());
  }

  @Test
  public void testAdocWithFiles() throws Exception {
    File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "test.jpg");
    file.deleteOnExit();
    file.createNewFile();

    String contentType = Files.probeContentType(file.toPath());
    assertThat(contentType, containsString("image"));

    FXPlatform.invokeLater(() -> {
      addThought.name.setText("test");
      addThought.fileViewController.addFiles(Arrays.asList(file));
    });

    activityController.save();
    activityController.waitForDataSource();

    persistentWork.run(session -> {
      List<Thought> thoughts = persistentWork.from(Thought.class);

      assertEquals(1, thoughts.size());

      Thought thought = thoughts.get(0);
      log.info(thought.getDescription());
      assertEquals(1, thought.getFiles().size());

      assertThat(thought.getDescription(), not(containsString("file://" + file.getAbsolutePath())));
    });

    Thought thought = persistentWork.from(Thought.class).iterator().next();
    persistentWork.remove(thought);

    assertFalse(thought.getPathInRepository().getParent().toFile().exists());

  }
}
