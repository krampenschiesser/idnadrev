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

package de.ks.idnadrev.thought.view;

import de.ks.LauncherRunner;
import de.ks.TempFileRule;
import de.ks.activity.ActivityController;
import de.ks.activity.ActivityHint;
import de.ks.idnadrev.entity.FileReference;
import de.ks.idnadrev.entity.Thought;
import de.ks.launch.JavaFXService;
import de.ks.launch.Launcher;
import de.ks.persistence.PersistentWork;
import de.ks.util.FXPlatform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class ViewThoughtsTest {
  private static final Logger log = LoggerFactory.getLogger(ViewThoughtsTest.class);
  @Rule
  public TempFileRule testFiles = new TempFileRule(2);
  private Scene scene;
  @Inject
  ActivityController controller;

  private ViewThoughts viewThoughts;

  @Before
  public void setUp() throws Exception {
    FXPlatform.waitForFX();
    PersistentWork.deleteAllOf(FileReference.class);
    PersistentWork.deleteAllOf(Thought.class);
    PersistentWork.persist(new Thought("test"));

    JavaFXService service = Launcher.instance.getService(JavaFXService.class);
    Stage stage = service.getStage();
    scene = stage.getScene();

    controller.startOrResume(new ActivityHint(ViewThoughtsActivity.class));
    controller.waitForTasks();

    viewThoughts = controller.getCurrentController();
  }

  @After
  public void tearDown() throws Exception {
    FXPlatform.waitForFX();
    controller.stop(ViewThoughtsActivity.class.getSimpleName());
  }

  @Test
  public void testDeleteThought() throws Exception {
    controller.waitForTasks();
    assertEquals(1, viewThoughts.thoughtTable.getItems().size());
    FXPlatform.invokeLater(() -> viewThoughts.thoughtTable.getSelectionModel().select(0));

    viewThoughts.delete();

    List<Thought> from = PersistentWork.from(Thought.class);
    assertEquals(0, from.size());
  }
}
