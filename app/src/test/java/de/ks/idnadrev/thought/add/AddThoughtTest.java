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

import de.ks.LauncherRunner;
import de.ks.TempFileRule;
import de.ks.activity.ActivityController;
import de.ks.activity.ActivityHint;
import de.ks.idnadrev.entity.FileReference;
import de.ks.idnadrev.entity.Thought;
import de.ks.launch.JavaFXService;
import de.ks.launch.Launcher;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.entity.Sequence;
import de.ks.text.command.InsertImage;
import de.ks.util.FXPlatform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class AddThoughtTest {
  private static final Logger log = LoggerFactory.getLogger(AddThoughtTest.class);
  @Rule
  public TempFileRule testFiles = new TempFileRule(2);
  private Scene scene;
  @Inject
  ActivityController controller;

  private AddThought addThought;

  @Before
  public void setUp() throws Exception {
    FXPlatform.waitForFX();
    PersistentWork.deleteAllOf(Sequence.class, FileReference.class);
    PersistentWork.deleteAllOf(Thought.class);

    JavaFXService service = Launcher.instance.getService(JavaFXService.class);
    Stage stage = service.getStage();
    scene = stage.getScene();
    controller.startOrResume(new ActivityHint(AddThoughtActivity.class));
    controller.waitForDataSource();

    addThought = controller.getCurrentController();
    FXPlatform.invokeLater(() -> {
      Clipboard.getSystemClipboard().clear();
    });
  }

  @After
  public void tearDown() throws Exception {
    FXPlatform.waitForFX();
    controller.stop(AddThoughtActivity.class.getSimpleName());
  }

  @Test
  public void testClipboard2LineString() throws Exception {
    String clipboardText = "title\nclipboard";
    copy2Clipboard(clipboardText);

    FXPlatform.waitForFX();
    assertEquals(clipboardText, addThought.description.getText());
    assertEquals("title", addThought.name.getText());
//    assertTrue(addThought.save.isFocused()); doesn't work anymore since validation is there

    FXPlatform.invokeLater(() -> {
      Clipboard.getSystemClipboard().clear();
      addThought.name.setText(null);
      addThought.description.setText(null);
    });
  }

  @Test
  public void testClipboardSingleLineString() throws Exception {
    FXPlatform.invokeLater(() -> {
      Clipboard clipboard = Clipboard.getSystemClipboard();
      clipboard.clear();
    });
    FXPlatform.waitForFX();
    String clipboardText = "singleClip";
    copy2Clipboard(clipboardText);
    FXPlatform.waitForFX();
    assertEquals(clipboardText, addThought.description.getText());
    assertEquals(clipboardText, addThought.name.getText());
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
    });
    Thread.sleep(50);
    controller.waitForTasks();
    FXPlatform.invokeLater(() -> {
      addThought.save.getOnAction().handle(new ActionEvent());
    });
    controller.waitForTasks();

    List<Thought> thoughts = PersistentWork.from(Thought.class);
    assertEquals(1, thoughts.size());

    Thought thought = thoughts.get(0);
    assertEquals("name", thought.getName());
    assertEquals("= name\n", thought.getDescription());
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
    controller.waitForDataSource();
    controller.getExecutorService().waitForAllTasksDone();


    List<Thought> thoughts = PersistentWork.from(Thought.class, thought -> thought.getFiles().size());
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

    assertFalse(addThought.name.textProperty().isEmpty().get());
    assertFalse(addThought.description.getText().isEmpty());
    assertEquals(2, addThought.fileViewController.getFiles().size());


    FXPlatform.invokeLater(() -> {
      MultipleSelectionModel<File> selectionModel = addThought.fileViewController.getFileList().getSelectionModel();
      selectionModel.clearSelection();
      selectionModel.select(0);
      addThought.fileViewController.removeFile(null);
      addThought.save.getOnAction().handle(new ActionEvent());
    });
    controller.getExecutorService().waitForAllTasksDone();


    List<Thought> thoughts = PersistentWork.from(Thought.class, thought -> thought.getFiles().size());
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
      addThought.fileViewController.addFiles(Arrays.asList(file));
    });

    InsertImage command = addThought.description.getCommand(InsertImage.class);
    for (Future<?> future : command.getSelectImageController().getLoadingFutures()) {
      future.get();
    }

    FXPlatform.invokeLater(() -> {
      GridPane grid = (GridPane) command.getSelectImageController().getImagePane().getChildren().get(0);
      Button node = (Button) grid.getChildren().get(0);
      node.getOnAction().handle(null);
    });

    FXPlatform.waitForFX();
    assertThat(addThought.description.getText(), containsString("test.jpg"));

    controller.save();
    Thread.sleep(100);
    controller.waitForDataSource();
    controller.waitForTasks();

    PersistentWork.wrap(() -> {
      List<FileReference> references = PersistentWork.from(FileReference.class);
      List<Thought> thoughts = PersistentWork.from(Thought.class);

      assertEquals(1, references.size());
      assertEquals(1, thoughts.size());

      Thought thought = thoughts.get(0);
      log.info(thought.getDescription());
      FileReference fileReference = references.get(0);

      assertEquals("test.jpg", fileReference.getName());
      assertEquals(thought, fileReference.getOwner());

      assertThat(thought.getDescription(), not(containsString("file://" + file.getAbsolutePath())));
      assertThat(thought.getDescription(), containsString(fileReference.getFileStorePath()));
      assertThat(thought.getDescription(), containsString(FileReference.FILESTORE_VAR));
    });

  }
}
