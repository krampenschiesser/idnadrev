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
import de.ks.application.fxml.DefaultLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class SelectImageControllerTest {

  private FlowPane imagesView;
  private SelectImageController controller;

  @Before
  public void setUp() throws Exception {
    DefaultLoader<FlowPane, SelectImageController> loader = new DefaultLoader<>(SelectImageController.class);
    loader.load();
    imagesView = loader.getView();
    controller = loader.getController();
  }

  @Test
  public void testShowImage() throws Exception {
    controller.addImage("keymap", "/de/ks/images/keymap.jpg").get();
    assertEquals(1, imagesView.getChildren().size());
    GridPane grid = (GridPane) imagesView.getChildren().get(0);
    assertEquals(2, grid.getChildren().size());

    controller.removeImage("keymap");
    assertEquals(0, imagesView.getChildren().size());
  }
}