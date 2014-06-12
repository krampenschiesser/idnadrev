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
package de.ks.idnadrev.thought.task;

import de.ks.FXPlatform;
import de.ks.LauncherRunner;
import de.ks.activity.ActivityController;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.WorkType;
import de.ks.persistence.PersistentWork;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.time.Duration;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(LauncherRunner.class)
public class ThoughtToTaskTest {
  @Inject
  ActivityController activityController;
  private MainTaskInfo controller;
  private ThoughtToTask thoughtToTask;

  @Before
  public void setUp() throws Exception {
    PersistentWork.deleteAllOf(Context.class, WorkType.class, Task.class);
    PersistentWork.persist(new Context("context"), new WorkType("physical"));

    activityController.start(ThoughtToTaskActivity.class);
    activityController.waitForDataSource();
    thoughtToTask = activityController.<ThoughtToTask>getCurrentController();
    controller = thoughtToTask.mainInfoController;
  }

  @After
  public void tearDown() throws Exception {
    activityController.stop(ThoughtToTaskActivity.class);
  }

  @Test
  public void testPersist() throws InterruptedException {
    FXPlatform.invokeLater(() -> {
      controller.name.setText("name");
      controller.contextController.getInput().setText("context");
      controller.workTypeController.getInput().setText("Physical");//upper case wanted
      controller.estimatedTimeDuration.setText("15min");
    });
    FXPlatform.invokeLater(() -> {
      controller.save();
    });
    activityController.waitForDataSource();

    List<Task> tasks = PersistentWork.from(Task.class, (t) -> {
      t.getContext().getName();
      t.getWorkType().getName();
    });
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals("name", task.getName());
    assertNotNull(task.getContext());
    assertEquals("context", task.getContext().getName());
    assertNotNull(task.getWorkType());
    assertEquals("physical", task.getWorkType().getName());

    Duration estimatedTime = task.getEstimatedTime();
    assertNotNull(estimatedTime);
    assertEquals(Duration.ofMinutes(15), estimatedTime);
  }
}
