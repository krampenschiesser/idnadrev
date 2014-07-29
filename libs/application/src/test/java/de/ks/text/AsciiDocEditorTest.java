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

import de.ks.JunitMatchers;
import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.activity.ActivityController;
import de.ks.activity.DummyTestDataSource;
import de.ks.application.Navigator;
import de.ks.launch.JavaFXService;
import de.ks.launch.Launcher;
import de.ks.text.command.InsertImage;
import de.ks.util.FXPlatform;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class AsciiDocEditorTest {
  @Inject
  ActivityController activityController;
  private AsciiDocEditor adocEditor;
  private ActivityCfg wrapper;

  @Before
  public void setUp() throws Exception {

    JavaFXService service = Launcher.instance.getService(JavaFXService.class);
    Navigator.registerWithBorderPane(service.getStage());


    wrapper = new ActivityCfg(DummyTestDataSource.class, AsciiDocEditor.class);
    activityController.start(wrapper);
    adocEditor = activityController.getControllerInstance(AsciiDocEditor.class);
  }

  @After
  public void tearDown() throws Exception {
    activityController.stop(wrapper);
  }

  @Test
  public void testShowHelp() throws Exception {
    Node current = activityController.getCurrentNode();
    assertNotNull(current);
    assertNotNull("needs scene", current.getScene());

    Platform.runLater(() -> adocEditor.showHelp());
    FXPlatform.waitForFX();
    assertTrue(adocEditor.helpDialog.getWindow().isShowing());

    Stage stage = (Stage) adocEditor.helpDialog.getWindow();
    assertEquals(Modality.NONE, stage.getModality());

    Platform.runLater(() -> adocEditor.helpDialog.hide());
    FXPlatform.waitForFX();
    assertFalse(adocEditor.helpDialog.getWindow().isShowing());
  }

  @Test
  public void testAdocParsing() throws Exception {
    FXPlatform.invokeLater(() -> adocEditor.editor.setText("= Title\n== more"));
    activityController.waitForTasks();
    assertNotNull(adocEditor.previewHtmlString);

    assertNull(adocEditor.preview.getEngine().getDocument());

    FXPlatform.invokeLater(() -> adocEditor.tabPane.getSelectionModel().select(1));
    JunitMatchers.withRetry(() -> adocEditor.preview.getEngine().getDocument() != null);
    assertNotNull(adocEditor.preview.getEngine().getDocument());

    FXPlatform.invokeLater(() -> adocEditor.tabPane.getSelectionModel().select(0));
    FXPlatform.waitForFX();
//    assertTrue(adocEditor.editor.isFocused()); FIXME doesn't work in jdk8u11 but jdk8u20
  }

  @Test
  public void testImageInsertion() throws Exception {
    adocEditor.getImages().add(new ImageData("test", "/de/ks/images/keymap.jpg"));

    InsertImage command = adocEditor.getCommand(InsertImage.class);
    SelectImageController selectImageController = command.getSelectImageController();
    for (Future<?> future : selectImageController.getLoadingFutures()) {
      future.get();
    }
    FXPlatform.waitForFX();
    assertEquals(1, selectImageController.getImagePane().getChildren().size());

    FXPlatform.invokeLater(() -> {
      GridPane grid = (GridPane) command.getSelectImageController().getImagePane().getChildren().get(0);
      grid.getChildren().get(0).getOnMouseClicked().handle(null);
    });

    assertThat(adocEditor.editor.getText(), Matchers.containsString("image::file:////de/ks/images/keymap.jpg"));
  }
}