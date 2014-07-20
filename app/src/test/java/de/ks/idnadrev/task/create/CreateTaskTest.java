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
import de.ks.activity.context.ActivityStore;
import de.ks.idnadrev.entity.*;
import de.ks.persistence.PersistentWork;
import de.ks.text.AsciiDocEditor;
import de.ks.util.FXPlatform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.time.Duration;
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

  @Before
  public void setUp() throws Exception {
    PersistentWork.deleteAllOf(WorkUnit.class, Task.class, Context.class, Tag.class, Thought.class);
    PersistentWork.persist(new Context("context"));

    activityController.start(CreateTaskActivity.class);
    activityController.waitForDataSource();
    createTask = activityController.<CreateTask>getCurrentController();
    controller = createTask.mainInfoController;
    expectedOutcomeEditor = createTask.expectedOutcomeController.expectedOutcome;
  }

  @After
  public void tearDown() throws Exception {
    activityController.stop(CreateTaskActivity.class);
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
      controller.contextController.getInput().setText("context");
      controller.estimatedTimeDuration.setText("15min");
      controller.funFactor.valueProperty().set(3);
      controller.mentalEffort.valueProperty().set(10);
      controller.physicalEffort.valueProperty().set(7);
      expectedOutcomeEditor.setText("outcome123");
    });
    activityController.waitForTasks();
    FXPlatform.waitForFX();
    FXPlatform.invokeLater(() -> {
      controller.save();
    });
    activityController.waitForDataSource();

    List<Task> tasks = PersistentWork.from(Task.class, (t) -> {
      t.getContext().getName();
    });
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals("name", task.getName());
    assertNotNull(task.getContext());
    assertEquals("context", task.getContext().getName());
    assertEquals(7, task.getPhysicalEffort().getAmount());
    assertEquals(3, task.getFunFactor().getAmount());
    assertEquals(10, task.getMentalEffort().getAmount());
    assertEquals("outcome123", task.getOutcome().getExpectedOutcome());

    Duration estimatedTime = task.getEstimatedTime();
    assertNotNull(estimatedTime);
    assertEquals(Duration.ofMinutes(15), estimatedTime);
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
      controller.save();
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
    activityController.getCurrentExecutorService().waitForAllTasksDone();
    FXPlatform.invokeLater(() -> {
      controller.tagAddController.getInput().setText("tag2");
      controller.tagAddController.getOnAction().handle(null);
    });
    activityController.getCurrentExecutorService().waitForAllTasksDone();

    FXPlatform.invokeLater(() -> {
      controller.save();
    });
    activityController.waitForDataSource();

    List<Task> tasks = PersistentWork.from(Task.class, (t) -> {
      t.getTags().toString();
    });
    Task task = tasks.get(0);
    assertEquals(2, task.getTags().size());
  }
}
