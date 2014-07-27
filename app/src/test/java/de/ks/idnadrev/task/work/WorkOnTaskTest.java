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

package de.ks.idnadrev.task.work;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityController;
import de.ks.activity.context.ActivityStore;
import de.ks.activity.link.NavigationHint;
import de.ks.idnadrev.entity.*;
import de.ks.idnadrev.task.view.ViewTasksActvity;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.entity.Sequence;
import de.ks.util.FXPlatform;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(LauncherRunner.class)
public class WorkOnTaskTest {
  @Inject
  ActivityController activityController;
  @Inject
  ActivityStore store;
  private WorkOnTask controller;

  @Before
  public void setUp() throws Exception {
    PersistentWork.deleteAllOf(FileReference.class, Sequence.class, WorkUnit.class, Task.class, Context.class, Tag.class);
    Context context = new Context("context");
    Task task = new Task("task").setProject(true);
    task.setContext(context);
    WorkUnit workUnit = new WorkUnit(task);
    workUnit.setStart(LocalDateTime.now().minus(7, ChronoUnit.MINUTES));
    workUnit.stop();
    task.setEstimatedTime(Duration.ofMinutes(10));
    PersistentWork.persist(context, task, workUnit);

    activityController.start(ViewTasksActvity.class);
    activityController.waitForDataSource();
    FXPlatform.waitForFX();
    NavigationHint hint = new NavigationHint(activityController.getCurrentActivity()).setDataSourceHint(() -> task);
    activityController.start(WorkOnTaskActivity.class, hint);
    activityController.waitForDataSource();
    controller = activityController.getControllerInstance(WorkOnTask.class);
  }

  @After
  public void tearDown() throws Exception {
    activityController.stop(WorkOnTaskActivity.class);
    activityController.stop(ViewTasksActvity.class);
  }

  @Test
  public void testStopWork() throws Exception {
    Thread.sleep(200);
    controller.stopWork();
    List<Task> tasks = PersistentWork.from(Task.class, t -> t.getWorkUnits().forEach(u -> u.getDuration()));
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals(2, task.getWorkUnits().size());
    assertThat(task.getWorkUnits().last().getSpentMillis(), Matchers.greaterThan(100L));
  }

  @Test
  public void testExistingWorkUnits() throws Exception {
    FXPlatform.waitForFX();
    assertThat(controller.estimatedTimeBar.getProgress(), Matchers.greaterThan(0.69));
  }
}