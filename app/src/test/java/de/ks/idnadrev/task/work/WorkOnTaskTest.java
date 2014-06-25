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

import de.ks.FXPlatform;
import de.ks.LauncherRunner;
import de.ks.activity.ActivityController;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Tag;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.WorkUnit;
import de.ks.idnadrev.task.view.ViewTasksActvity;
import de.ks.persistence.PersistentWork;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(LauncherRunner.class)
public class WorkOnTaskTest {
  @Inject
  ActivityController activityController;
  private WorkOnTask controller;

  @Before
  public void setUp() throws Exception {
    PersistentWork.deleteAllOf(WorkUnit.class, Task.class, Context.class, Tag.class);
    Context context = new Context("context");
    Task project1 = new Task("project1").setProject(true);
    project1.setContext(context);
    PersistentWork.persist(context, project1);

    activityController.start(ViewTasksActvity.class);
    activityController.waitForDataSource();
    FXPlatform.waitForFX();
    activityController.start(WorkOnTaskActivity.class, t -> project1, null);
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
    assertEquals(1, task.getWorkUnits().size());
    assertThat(task.getWorkUnits().first().getSpentMillis(), Matchers.greaterThan(100L));
  }
}