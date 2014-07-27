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
package de.ks.idnadrev.task.view;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityController;
import de.ks.idnadrev.entity.*;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.entity.Sequence;
import de.ks.util.FXPlatform;
import javafx.scene.control.TreeItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(LauncherRunner.class)
public class TaskOverViewTest {
  @Inject
  ActivityController activityController;
  private ViewTasks controller;

  @Before
  public void setUp() throws Exception {
    PersistentWork.deleteAllOf(FileReference.class, Sequence.class, WorkUnit.class, Task.class, Context.class, Tag.class);

    Context context = new Context("context");

    Task project1 = new Task("project1").setProject(true);
    project1.setContext(context);
    for (int i = 0; i < 5; i++) {
      Task task = new Task("task" + i);
      project1.addChild(task);
    }
    Task finished = new Task("finished");
    finished.setFinished(true);
    project1.addChild(finished);

    Task other = new Task("other").setContext(context);

    PersistentWork.persist(context, project1, other);

    activityController.start(ViewTasksActvity.class);
    activityController.waitForDataSource();
    controller = activityController.getControllerInstance(ViewTasks.class);
  }

  @After
  public void tearDown() throws Exception {
    activityController.stop(ViewTasksActvity.class);
    FXPlatform.waitForFX();
  }

  @Test
  public void testBinding() throws Exception {
    TreeItem<Task> root = controller.tasksView.getRoot();
    assertNotNull(root);
    assertEquals(2, root.getChildren().size());
    assertEquals("other", root.getChildren().get(0).getValue().getName());
    assertEquals("project1", root.getChildren().get(1).getValue().getName());

    assertEquals(5, root.getChildren().get(1).getChildren().size());
  }

  @Test
  public void testDeleteProject() throws Exception {
    List<Task> from = PersistentWork.from(Task.class);
    assertEquals(8, from.size());

    TreeItem<Task> project = controller.tasksView.getRoot().getChildren().get(1);
    FXPlatform.invokeLater(() -> controller.tasksView.getSelectionModel().select(project));

    controller.deleteTask();

    from = PersistentWork.from(Task.class);
    assertEquals(1, from.size());
  }
}
