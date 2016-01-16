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

import de.ks.flatjsondb.PersistentWork;
import de.ks.idnadrev.IdnadrevIntegrationTestModule;
import de.ks.idnadrev.IdnadrevWindow;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.Thought;
import de.ks.idnadrev.entity.WorkUnit;
import de.ks.idnadrev.task.view.ViewTasksActvity;
import de.ks.idnadrev.thought.add.AddThought;
import de.ks.idnadrev.thought.add.AddThoughtActivity;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.activity.ActivityController;
import de.ks.standbein.activity.ActivityHint;
import de.ks.util.FXPlatform;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static de.ks.standbein.JunitMatchers.withRetry;
import static org.junit.Assert.*;

public class WorkOnTaskTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IdnadrevIntegrationTestModule()).launchServices();
  @Inject
  ActivityController activityController;
  @Inject
  IdnadrevWindow window;
  @Inject
  PersistentWork persistentWork;

  private WorkOnTask controller;

  @Before
  public void setUp() throws Exception {
    Context context = new Context("context");
    Task task = new Task("task").setProject(true);
    task.setContext(context);
    WorkUnit workUnit = task.start();
    workUnit.setStart(LocalDateTime.now().minus(7, ChronoUnit.MINUTES));
    workUnit.stop();
    task.setEstimatedTime(Duration.ofMinutes(10));
    persistentWork.persist(context, task);

    activityController.startOrResume(new ActivityHint(ViewTasksActvity.class));
    activityController.waitForTasks();
    FXPlatform.waitForFX();
    ActivityHint hint = new ActivityHint(WorkOnTaskActivity.class, activityController.getCurrentActivityId()).setDataSourceHint(() -> task);
    activityController.startOrResume(hint);
    activityController.waitForTasks();
    controller = activityController.getControllerInstance(WorkOnTask.class);
  }

  @After
  public void tearDown() throws Exception {
    activityController.stopAll();
  }

  @Test
  public void testStopWork() throws Exception {
    Thread.sleep(200);
    FXPlatform.invokeLater(() -> controller.description.setText("desc"));
    controller.stopWork();
    activityController.waitForTasks();
    List<Task> tasks = persistentWork.from(Task.class);
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals(2, task.getWorkUnits().size());
    assertThat(task.getWorkUnits().last().getSpentMillis(), Matchers.greaterThan(100L));
    assertEquals("desc", task.getDescription());
  }

  @Test
  public void testContinueOnSuspend() throws Exception {
    Thread.sleep(200);
    FXPlatform.invokeLater(() -> controller.createThought());
    FXPlatform.waitForFX();
    activityController.waitForTasks();

    withRetry(() -> AddThoughtActivity.class.getSimpleName().equals(activityController.getCurrentActivityId()));
    activityController.waitForTasks();
    assertEquals(AddThoughtActivity.class.getSimpleName(), activityController.getCurrentActivityId());

    assertEquals(1, window.getProgressBox().getChildren().size());
    assertTrue(window.getProgressBox().getChildren().get(0).isVisible());
    assertNotNull(window.getWorkingOnTaskLink().getCurrentTask());
    assertEquals("task", window.getWorkingOnTaskLink().getCurrentTask().getName());

    AddThought addThought = activityController.getControllerInstance(AddThought.class);
    FXPlatform.invokeLater(() -> addThought.getName().setText("testThought"));
    FXPlatform.invokeLater(() -> addThought.getSave().getOnAction().handle(null));
    FXPlatform.waitForFX();
    activityController.waitForTasks();

    withRetry(() -> WorkOnTaskActivity.class.getSimpleName().equals(activityController.getCurrentActivityId()));
    activityController.waitForTasks();
    assertEquals(WorkOnTaskActivity.class.getSimpleName(), activityController.getCurrentActivityId());

    List<Thought> thoughts = persistentWork.from(Thought.class);
    assertEquals(1, thoughts.size());
    assertEquals("testThought", thoughts.get(0).getName());
    List<Task> tasks = persistentWork.from(Task.class);
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals(2, task.getWorkUnits().size());
  }

  @Test
  public void testGoBack() throws Exception {
    FXPlatform.invokeLater(() -> controller.createThought());
    FXPlatform.waitForFX();
    activityController.waitForTasks();

    withRetry(() -> AddThoughtActivity.class.getSimpleName().equals(activityController.getCurrentActivityId()));
    activityController.waitForTasks();
    assertEquals(AddThoughtActivity.class.getSimpleName(), activityController.getCurrentActivityId());

    assertNotNull(window.getWorkingOnTaskLink().getCurrentTask());
    assertEquals("task", window.getWorkingOnTaskLink().getCurrentTask().getName());

    FXPlatform.invokeLater(() -> window.getWorkingOnTaskLink().getHyperlink().getOnAction().handle(null));

    withRetry(() -> WorkOnTaskActivity.class.getSimpleName().equals(activityController.getCurrentActivityId()));
    activityController.waitForTasks();
    assertEquals(WorkOnTaskActivity.class.getSimpleName(), activityController.getCurrentActivityId());
  }

  @Test
  public void testExistingWorkUnits() throws Exception {
    FXPlatform.waitForFX();
    assertThat(controller.estimatedTimeBar.getProgress(), Matchers.greaterThan(0.69));
  }

}