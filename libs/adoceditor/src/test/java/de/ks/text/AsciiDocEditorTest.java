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

package de.ks.text;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.ks.Condition;
import de.ks.JunitMatchers;
import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.activity.ActivityController;
import de.ks.activity.ActivityHint;
import de.ks.application.Navigator;
import de.ks.executor.group.LastTextChange;
import de.ks.launch.ApplicationService;
import de.ks.launch.Launcher;
import de.ks.text.command.InsertImage;
import de.ks.text.image.GlobalImageProvider;
import de.ks.text.image.ImageData;
import de.ks.text.image.SelectImageController;
import de.ks.util.FXPlatform;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebEngine;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class AsciiDocEditorTest {
  @Inject
  ActivityController activityController;
  @Inject
  GlobalImageProvider imageProvider;
  private AsciiDocEditor adocEditor;
  private ActivityCfg wrapper;

  @Before
  public void setUp() throws Exception {

    ApplicationService service = Launcher.instance.getService(ApplicationService.class);
    Navigator.registerWithBorderPane(service.getStage());


    activityController.startOrResume(new ActivityHint(AscidoctTestActivity.class));
    adocEditor = activityController.getControllerInstance(AsciiDocEditor.class);
  }

  @After
  public void tearDown() throws Exception {
    activityController.stopAll();
  }

  @Test
  public void testAdocParsing() throws Exception {
    FXPlatform.invokeLater(() -> adocEditor.editor.setText("= Title\n== more"));
    FXPlatform.invokeLater(() -> adocEditor.tabPane.getSelectionModel().select(1));
    JunitMatchers.withRetry(() -> adocEditor.preview.getCurrentHtml() != null);
    assertNotNull("preview string is null", adocEditor.preview.getCurrentHtml());

    WebEngine engine = adocEditor.preview.getWebView().getEngine();
    JunitMatchers.withRetry(() -> adocEditor.preview.getWebView().getEngine() != null);
    Condition.waitFor5s(() -> engine.getDocument(), Matchers.nullValue());

    FXPlatform.invokeLater(() -> adocEditor.tabPane.getSelectionModel().select(1));
    JunitMatchers.withRetry(() -> engine.getDocument() != null);
    assertNotNull("document did not load, is still null", engine.getDocument());

    FXPlatform.invokeLater(() -> adocEditor.tabPane.getSelectionModel().select(0));
    FXPlatform.waitForFX();
//    assertTrue(adocEditor.editor.isFocused()); FIXME doesn't work in jdk8u11 but jdk8u20
  }

  @Test
  public void testImageInsertion() throws Exception {
    imageProvider.addImage(new ImageData("test", "/de/ks/images/keymap.jpg"));

    InsertImage command = adocEditor.getCommand(InsertImage.class);
    command.collectImages();
    SelectImageController selectImageController = command.getSelectImageController();
    for (Future<?> future : selectImageController.getLoadingFutures()) {
      future.get();
    }
    activityController.waitForTasks();
    FXPlatform.waitForFX();
    assertEquals(1, selectImageController.getImagePane().getChildren().size());

    FXPlatform.invokeLater(() -> {
      GridPane grid = (GridPane) command.getSelectImageController().getImagePane().getChildren().get(0);
      Button node = (Button) grid.getChildren().get(0);
      node.getOnAction().handle(null);
    });

    assertThat(adocEditor.editor.getText(), Matchers.containsString("image::file:////de/ks/images/keymap.jpg"));
  }

  @Test
  public void testPersistentStoreBack() throws Exception {
    File file = new File(System.getProperty("java.io.tmpdir"));
    adocEditor.setPersistentStoreBack("test", file);
    FXPlatform.invokeLater(() -> adocEditor.setText("bla blubb"));
    Thread.sleep(LastTextChange.WAIT_TIME * 2);
    activityController.waitForTasks();

    List<String> strings = Files.readLines(new File(file, "test"), Charsets.UTF_8);
    assertEquals(1, strings.size());

    adocEditor.removePersistentStoreBack();
    FXPlatform.invokeLater(() -> adocEditor.setText(""));
    Thread.sleep(LastTextChange.WAIT_TIME * 2);
    activityController.waitForTasks();

    adocEditor.setPersistentStoreBack("test", file);
    FXPlatform.invokeLater(() -> adocEditor.onRefresh(null));

    activityController.waitForTasks();
    assertEquals("bla blubb\n", adocEditor.getText());
  }

  @Test
  public void testSearching() throws Exception {
    FXPlatform.invokeLater(() -> {
      adocEditor.searchField.setText("b");
      adocEditor.setText("a\na\na\nb\nb\na\nb");
      adocEditor.searchForText();
    });
    activityController.waitForTasks();
    FXPlatform.waitForFX();
    assertEquals(6, adocEditor.editor.getCaretPosition());

    FXPlatform.invokeLater(() -> adocEditor.searchForText());
    activityController.waitForTasks();
    FXPlatform.waitForFX();
    assertEquals(8, adocEditor.editor.getCaretPosition());

    FXPlatform.invokeLater(() -> adocEditor.searchForText());
    activityController.waitForTasks();
    FXPlatform.waitForFX();
    assertEquals(12, adocEditor.editor.getCaretPosition());

    FXPlatform.invokeLater(() -> adocEditor.searchForText());
    activityController.waitForTasks();
    FXPlatform.waitForFX();
    assertEquals(6, adocEditor.editor.getCaretPosition());
  }

  @Test
  public void testSearchingEmptyText() throws Exception {
    FXPlatform.invokeLater(() -> {
      adocEditor.searchField.setText("b");
      adocEditor.setText(null);
      adocEditor.searchForText();
    });
    activityController.waitForTasks();
    assertEquals(0, adocEditor.editor.getCaretPosition());

    FXPlatform.invokeLater(() -> adocEditor.searchForText());
    activityController.waitForTasks();
    assertEquals(0, adocEditor.editor.getCaretPosition());
  }
}