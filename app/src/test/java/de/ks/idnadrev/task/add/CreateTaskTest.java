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
package de.ks.idnadrev.task.add;

import de.ks.flatadocdb.session.Session;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.IdnadrevIntegrationTestModule;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.TaskState;
import de.ks.idnadrev.entity.Thought;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.activity.ActivityCfg;
import de.ks.standbein.activity.ActivityController;
import de.ks.standbein.activity.context.ActivityStore;
import de.ks.texteditor.TextEditor;
import de.ks.util.FXPlatform;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class CreateTaskTest extends ActivityTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IdnadrevIntegrationTestModule()).launchServices();

  @Inject
  ActivityController activityController;
  @Inject
  ActivityStore store;
  private MainTaskInfo controller;
  private CreateTask createTask;
  private TextEditor expectedOutcomeEditor;
  private EffortInfo effortInfo;
  private TaskSchedule taskSchedule;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return CreateTaskActivity.class;
  }

  @Override
  protected void createTestData(Session session) {
    session.persist(new Context("context"));
  }

  @Before
  public void setUp() throws Exception {
    createTask = activityController.<CreateTask>getCurrentController();
    controller = createTask.mainInfoController;
    effortInfo = activityController.getControllerInstance(EffortInfo.class);
    taskSchedule = activityController.getControllerInstance(TaskSchedule.class);
    expectedOutcomeEditor = createTask.expectedOutcomeController.expectedOutcome;
  }

  @Test
  public void testTaskFromThought() throws Exception {
    Thought bla = new Thought("Bla").setDescription("description");
    persistentWork.persist(bla);

    @SuppressWarnings("unchecked")
    CreateTaskDS datasource = (CreateTaskDS) store.getDatasource();
    datasource.fromThought = bla;
    activityController.reload();
    activityController.waitForTasks();
    FXPlatform.waitForFX();
    assertEquals("Bla", controller.name.getText());
    assertEquals("description", controller.description.getText());

    activityController.save();
    activityController.waitForTasks();

    assertNull(datasource.fromThought);
    assertEquals(0, persistentWork.from(Thought.class).size());
  }

  @Test
  public void testProjectFromThought() throws Exception {
    Thought bla = new Thought("Bla").setDescription("description");
    persistentWork.persist(bla);

    @SuppressWarnings("unchecked")
    CreateTaskDS datasource = (CreateTaskDS) store.getDatasource();
    datasource.fromThought = bla;
    activityController.reload();
    activityController.waitForTasks();
    FXPlatform.waitForFX();
    assertEquals("Bla", controller.name.getText());
    assertEquals("description", controller.description.getText());
    FXPlatform.invokeLater(() -> controller.project.setSelected(true));

    FXPlatform.invokeLater(() -> createTask.save());
    activityController.waitForTasks();

    assertEquals(CreateTaskActivity.class.getSimpleName(), activityController.getCurrentActivityId());
    FXPlatform.invokeLater(() -> controller.name.setText("a real action"));

    FXPlatform.invokeLater(() -> createTask.save());
    activityController.waitForTasks();

    assertNull(datasource.fromThought);
    assertEquals(0, persistentWork.from(Thought.class).size());
    assertEquals(2, persistentWork.count(Task.class));
  }

  @Test
  public void testPersist() throws InterruptedException {
    FXPlatform.invokeLater(() -> {
      controller.name.setText("name");
      controller.description.setText("description");
      controller.contextController.getTextField().setText("context");
      controller.estimatedTimeDuration.setText("15min");
      controller.state.setValue(TaskState.ASAP);
      effortInfo.funFactor.valueProperty().set(3);
      effortInfo.mentalEffort.valueProperty().set(5);
      effortInfo.physicalEffort.valueProperty().set(3);
      taskSchedule.dueDate.setValue(LocalDate.now());
      taskSchedule.dueTime.setText("11:35");
      expectedOutcomeEditor.setText("outcome123");
    });
    Thread.sleep(150);
    activityController.waitForTasks();
    FXPlatform.waitForFX();
    FXPlatform.invokeLater(() -> {
      createTask.save();
    });
    activityController.waitForTasks();

    List<Task> tasks = persistentWork.from(Task.class);
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals("name", task.getName());
    assertEquals(TaskState.ASAP, task.getState());
    assertEquals("description", task.getDescription());
    assertNotNull(task.getContext());
    assertEquals("context", task.getContext().getName());
    assertEquals(3, task.getPhysicalEffort().getAmount());
    assertEquals(3, task.getFunFactor().getAmount());
    assertEquals(5, task.getMentalEffort().getAmount());
    assertEquals("outcome123", task.getOutcome().getExpectedOutcome());
    assertNotNull(task.getSchedule());
    assertEquals(LocalTime.of(11, 35), task.getSchedule().getScheduledTime());
    assertEquals(LocalDate.now(), task.getSchedule().getScheduledDate());

    Duration estimatedTime = task.getEstimatedTime();
    assertNotNull(estimatedTime);
    assertEquals(Duration.ofMinutes(15), estimatedTime);
  }

  @Test
  public void testPersistProposedWeek() throws InterruptedException {
    FXPlatform.invokeLater(() -> {
      controller.name.setText("name");
      taskSchedule.proposedWeek.setValue(LocalDate.of(2014, 1, 3));
      taskSchedule.useProposedWeekDay.setSelected(true);
    });
    activityController.waitForTasks();
    FXPlatform.waitForFX();
    FXPlatform.invokeLater(() -> {
      createTask.save();
    });
    activityController.waitForTasks();

    List<Task> tasks = persistentWork.from(Task.class);
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals("name", task.getName());
    assertNotNull(task.getSchedule());
    assertEquals(1, task.getSchedule().getProposedWeek());
    assertEquals(DayOfWeek.FRIDAY, task.getSchedule().getProposedWeekDay());
  }

  @Test
  public void testSaveProject() throws InterruptedException {
    createTask("name", controller -> controller.project.setSelected(true));

    List<Task> tasks = persistentWork.from(Task.class);
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals("name", task.getName());
    assertTrue(task.isProject());
  }

  protected void createTask(String name, Consumer<MainTaskInfo> consumer) throws InterruptedException {
    FXPlatform.waitForFX();
    activityController.waitForDataSource();
    FXPlatform.invokeLater(() -> {
      controller.name.setText(name);
      consumer.accept(controller);
    });
    Thread.sleep(150);
    activityController.waitForTasks();
    FXPlatform.waitForFX();
    FXPlatform.invokeLater(() -> {
      createTask.save();
    });
    activityController.waitForDataSource();
  }

  @Test
  public void testSaveWithParentProject() throws InterruptedException {
    createTask("parent", controller -> controller.project.setSelected(true));
    createTask("child", controller -> controller.parentProjectController.getTextField().setText("parent"));

    persistentWork.run(session -> {
      List<Task> tasks = persistentWork.from(Task.class);
      assertEquals(2, tasks.size());
      for (Task task : tasks) {
        if (task.getName().equals("parent")) {
          assertTrue(task.isProject());
          assertEquals(1, task.getChildren().size());
          assertNull(task.getParent());
        } else {
          assertFalse(task.isProject());
          assertEquals(0, task.getChildren().size());
          assertNotNull(task.getParent());
        }
      }
    });
  }

  @Test
  public void testTags() throws Exception {
    FXPlatform.invokeLater(() -> {
      controller.name.setText("name");

      controller.tagAddController.getInput().setText("tag1");
      controller.tagAddController.getOnAction().handle(null);
    });
    activityController.getExecutorService().waitForAllTasksDone();
    FXPlatform.invokeLater(() -> {
      controller.tagAddController.getInput().setText("tag2");
      controller.tagAddController.getOnAction().handle(null);
    });
    activityController.getExecutorService().waitForAllTasksDone();

    FXPlatform.invokeLater(() -> {
      createTask.save();
    });
    activityController.waitForTasks();

    List<Task> tasks = persistentWork.from(Task.class);
    Task task = tasks.get(0);
    assertEquals(2, task.getTags().size());
  }

  @Test
  public void testEditExisting() throws Exception {
    Task bla = new Task("Bla").setDescription("description");
    persistentWork.persist(bla);

    @SuppressWarnings("unchecked")
    CreateTaskDS datasource = (CreateTaskDS) store.getDatasource();
    datasource.fromTask = bla;
    activityController.reload();
    activityController.waitForTasks();
    FXPlatform.waitForFX();
    assertEquals("Bla", controller.name.getText());
    assertEquals("description", controller.description.getText());

    FXPlatform.invokeLater(() -> controller.name.setText("blubb"));
    FXPlatform.invokeLater(() -> controller.description.setText("hallo"));
    FXPlatform.waitForFX();

    activityController.save();
    activityController.waitForTasks();

    assertNull(datasource.fromTask);
    List<Task> tasks = persistentWork.from(Task.class);
    assertEquals(1, tasks.size());
    assertEquals("hallo", tasks.get(0).getDescription());
    assertEquals("blubb", tasks.get(0).getName());
  }

}
