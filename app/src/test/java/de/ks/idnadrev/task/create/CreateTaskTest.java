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
package de.ks.idnadrev.task.create;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityController;
import de.ks.activity.ActivityHint;
import de.ks.activity.context.ActivityStore;
import de.ks.idnadrev.entity.*;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.entity.Sequence;
import de.ks.scheduler.Schedule;
import de.ks.text.AsciiDocEditor;
import de.ks.util.FXPlatform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class CreateTaskTest {
  @Inject
  ActivityController activityController;
  @Inject
  ActivityStore store;
  private MainTaskInfo controller;
  private CreateTask createTask;
  private AsciiDocEditor expectedOutcomeEditor;
  private EffortInfo effortInfo;
  private TaskSchedule taskSchedule;

  @Before
  public void setUp() throws Exception {
    PersistentWork.deleteAllOf(FileReference.class, Sequence.class, WorkUnit.class, Task.class, Schedule.class, Context.class, Tag.class, Thought.class);
    PersistentWork.persist(new Context("context"));

    activityController.startOrResume(new ActivityHint(CreateTaskActivity.class));
    activityController.waitForDataSource();
    createTask = activityController.<CreateTask>getCurrentController();
    controller = createTask.mainInfoController;
    effortInfo = activityController.getControllerInstance(EffortInfo.class);
    taskSchedule = activityController.getControllerInstance(TaskSchedule.class);
    expectedOutcomeEditor = createTask.expectedOutcomeController.expectedOutcome;
  }

  @After
  public void tearDown() throws Exception {
    activityController.stop(CreateTaskActivity.class.getSimpleName());
  }

  @Test
  public void testTaskFromThought() throws Exception {
    Thought bla = new Thought("Bla").setDescription("description");
    PersistentWork.persist(bla);

    @SuppressWarnings("unchecked") CreateTaskDS datasource = (CreateTaskDS) store.getDatasource();
    datasource.fromThought = bla;
    activityController.reload();
    activityController.waitForDataSource();
    FXPlatform.waitForFX();
    assertEquals("Bla", controller.name.getText());
    assertEquals("description", controller.description.getText());

    activityController.save();
    activityController.waitForDataSource();

    assertNull(datasource.fromThought);
    assertEquals(0, PersistentWork.from(Thought.class).size());
  }

  @Test
  public void testPersist() throws InterruptedException {
    FXPlatform.invokeLater(() -> {
      controller.name.setText("name");
      controller.description.setText("description");
      controller.contextController.getInput().setText("context");
      controller.estimatedTimeDuration.setText("15min");
      controller.state.setValue(TaskState.ASAP);
      effortInfo.funFactor.valueProperty().set(3);
      effortInfo.mentalEffort.valueProperty().set(5);
      effortInfo.physicalEffort.valueProperty().set(3);
      taskSchedule.dueDate.setValue(LocalDate.now());
      taskSchedule.dueTime.setText("11:35");
      expectedOutcomeEditor.setText("outcome123");
    });
    activityController.waitForTasks();
    FXPlatform.waitForFX();
    FXPlatform.invokeLater(() -> {
      createTask.save();
    });
    activityController.waitForDataSource();

    List<Task> tasks = PersistentWork.from(Task.class, (t) -> {
      t.getContext().getName();
      if (t.getSchedule() != null) {
        t.getSchedule().getScheduledDate();
      }
    });
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
    activityController.waitForDataSource();

    List<Task> tasks = PersistentWork.from(Task.class, (t) -> {
      if (t.getSchedule() != null) {
        t.getSchedule().getScheduledDate();
      }
    });
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

    List<Task> tasks = PersistentWork.from(Task.class);
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals("name", task.getName());
    assertTrue(task.isProject());
  }

  protected void createTask(String name, Consumer<MainTaskInfo> consumer) {
    FXPlatform.invokeLater(() -> {
      controller.name.setText(name);
      consumer.accept(controller);
    });
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
    createTask("child", controller -> controller.parentProjectController.getInput().setText("parent"));

    PersistentWork.wrap(() -> {
      List<Task> tasks = PersistentWork.from(Task.class);
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
    PersistentWork.persist(new Tag("tag1"));

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
    activityController.waitForDataSource();

    List<Task> tasks = PersistentWork.from(Task.class, (t) -> {
      t.getTags().toString();
    });
    Task task = tasks.get(0);
    assertEquals(2, task.getTags().size());
  }

  @Test
  public void testEditExisting() throws Exception {
    Task bla = new Task("Bla").setDescription("description");
    PersistentWork.persist(bla);

    @SuppressWarnings("unchecked") CreateTaskDS datasource = (CreateTaskDS) store.getDatasource();
    datasource.fromTask = bla;
    activityController.reload();
    activityController.waitForDataSource();
    FXPlatform.waitForFX();
    assertEquals("Bla", controller.name.getText());
    assertEquals("description", controller.description.getText());

    FXPlatform.invokeLater(() -> controller.name.setText("blubb"));
    FXPlatform.invokeLater(() -> controller.description.setText("hallo"));
    FXPlatform.waitForFX();

    activityController.save();
    activityController.waitForDataSource();

    assertNull(datasource.fromTask);
    List<Task> tasks = PersistentWork.from(Task.class);
    assertEquals(1, tasks.size());
    assertEquals("hallo", tasks.get(0).getDescription());
    assertEquals("blubb", tasks.get(0).getName());
  }

}
