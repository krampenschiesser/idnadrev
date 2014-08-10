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

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.activity.ActivityController;
import de.ks.activity.ActivityHint;
import de.ks.application.Navigator;
import de.ks.launch.JavaFXService;
import de.ks.launch.Launcher;
import de.ks.util.FXPlatform;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class SelectImageControllerTest {
  @Inject
  ActivityController activityController;
  private ActivityCfg wrapper;

  private FlowPane imagesView;
  private SelectImageController controller;

  @Before
  public void setUp() throws Exception {
    JavaFXService service = Launcher.instance.getService(JavaFXService.class);
    Navigator.registerWithBorderPane(service.getStage());

    activityController.start(new ActivityHint(SelectImageActivity.class));

    controller = activityController.getControllerInstance(SelectImageController.class);
    imagesView = controller.getImagePane();
  }

  @After
  public void tearDown() throws Exception {
    activityController.stopAll();
  }

  @Test
  public void testShowImage() throws Exception {
    controller.addImage("keymap", "/de/ks/images/keymap.jpg").get();
    FXPlatform.waitForFX();
    assertEquals(1, imagesView.getChildren().size());
    GridPane grid = (GridPane) imagesView.getChildren().get(0);
    assertEquals(2, grid.getChildren().size());

    FXPlatform.invokeLater(() -> controller.removeImage("keymap"));
    assertEquals(0, imagesView.getChildren().size());
  }
}