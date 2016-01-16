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

import de.ks.flatadocdb.session.Session;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.IdnadrevIntegrationTestModule;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.task.create.CreateTaskActivity;
import de.ks.idnadrev.task.create.MainTaskInfo;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.activity.ActivityCfg;
import de.ks.standbein.activity.ActivityController;
import de.ks.util.FXPlatform;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

import static de.ks.standbein.JunitMatchers.withRetry;
import static org.junit.Assert.*;

public class ViewTasksTest extends ActivityTest {
  private static final Logger log = LoggerFactory.getLogger(ViewTasksTest.class);

  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IdnadrevIntegrationTestModule()).launchServices();

  @Inject
  ActivityController activityController;
  private ViewTasks controller;
  private TreeTableView<Task> tasksView;
  private ViewTasksMaster master;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return ViewTasksActvity.class;
  }

  @Override
  protected void createTestData(Session session) {
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
    Task noContext = new Task("noContext");

    persistentWork.persist(context, project1, other, noContext);
  }

  @Before
  public void setUp() throws Exception {
    controller = activityController.getControllerInstance(ViewTasks.class);
    master = activityController.getControllerInstance(ViewTasksMaster.class);
    tasksView = master.getTasksView();
  }

  @Test
  public void testBinding() throws Exception {
    TreeItem<Task> root = tasksView.getRoot();
    assertNotNull(root);
    assertEquals(3, root.getChildren().size());
    assertEquals("noContext", root.getChildren().get(0).getValue().getName());
    assertEquals("other", root.getChildren().get(1).getValue().getName());
    assertEquals("project1", root.getChildren().get(2).getValue().getName());

    assertEquals(5, root.getChildren().get(2).getChildren().size());
  }

  @Test
  public void testDeleteProject() throws Exception {
    List<Task> from = persistentWork.from(Task.class);
    assertEquals(9, from.size());

    TreeItem<Task> project = tasksView.getRoot().getChildren().get(2);
    FXPlatform.invokeLater(() -> tasksView.getSelectionModel().select(project));

    controller.deleteTask();

    from = persistentWork.from(Task.class);
    assertEquals(2, from.size());
  }

  @Test
  public void testFilter() throws Exception {
    FXPlatform.invokeLater(() -> master.searchField.setText("4"));

    TreeItem<Task> root = tasksView.getRoot();

    ObservableList<TreeItem<Task>> children = root.getChildren();
    assertEquals(1, children.size());
    assertEquals("project1", children.get(0).getValue().getName());

    children = children.get(0).getChildren();
    assertEquals(1, children.size());
    assertEquals("task4", children.get(0).getValue().getName());
  }

  @Test
  public void testContextFiltering() throws Exception {
    FXPlatform.invokeLater(() -> master.contextSelection.setValue(localized.get("all")));

    TreeItem<Task> root = tasksView.getRoot();

    ObservableList<TreeItem<Task>> children = root.getChildren();
    assertEquals(3, children.size());

    log.info("Filtering with context");
    FXPlatform.invokeLater(() -> master.contextSelection.setValue("context"));
    root = tasksView.getRoot();
    children = root.getChildren();
    assertEquals(children.toString(), 2, children.size());

    log.info("Filtering with NO context");
    FXPlatform.invokeLater(() -> master.contextSelection.setValue(""));
    root = tasksView.getRoot();
    children = root.getChildren();
    assertEquals(children.toString(), 2, children.size());
  }

  @Test
  public void testCreateSubtaskFromProject() throws Exception {
    TreeItem<Task> project = tasksView.getRoot().getChildren().get(2);//they are sorted
    Task value = project.getValue();
    assertEquals("project1", value.getName());

    FXPlatform.invokeLater(() -> tasksView.getSelectionModel().select(project));
    FXPlatform.invokeLater(() -> controller.createSubtask());

    withRetry(() -> CreateTaskActivity.class.getSimpleName().equals(activityController.getCurrentActivityId()));
    activityController.waitForTasks();

    log.info("Retrieving {}", MainTaskInfo.class.getSimpleName());
    MainTaskInfo mainTaskInfo = activityController.getControllerInstance(MainTaskInfo.class);
    FXPlatform.invokeLater(() -> mainTaskInfo.getName().setText("steak"));
    activityController.save();

    withRetry(() -> ViewTasksActvity.class.getSimpleName().equals(activityController.getCurrentActivityId()));
    activityController.waitForTasks();
    persistentWork.run(session -> {
      Task child = persistentWork.forName(Task.class, "steak");
      assertNotNull(child.getParent());
      assertEquals("project1", child.getParent().getName());
    });
  }

  @Test
  public void testCreateSubtaskFromTask() throws Exception {
    TreeItem<Task> other = tasksView.getRoot().getChildren().get(0);//they are sorted
    Task value = other.getValue();
    assertEquals("noContext", value.getName());

    FXPlatform.invokeLater(() -> tasksView.getSelectionModel().select(other));
    FXPlatform.invokeLater(() -> controller.createSubtask());

    withRetry(() -> CreateTaskActivity.class.getSimpleName().equals(activityController.getCurrentActivityId()));
    activityController.waitForTasks();

    log.info("Retrieving {}", MainTaskInfo.class.getSimpleName());
    MainTaskInfo mainTaskInfo = activityController.getControllerInstance(MainTaskInfo.class);
    FXPlatform.invokeLater(() -> mainTaskInfo.getName().setText("steak"));
    activityController.save();

    withRetry(() -> ViewTasksActvity.class.getSimpleName().equals(activityController.getCurrentActivityId()));
    activityController.waitForTasks();
    persistentWork.run(session -> {
      Task child = persistentWork.forName(Task.class, "steak");
      Task parent = child.getParent();
      assertNotNull(parent);
      assertEquals("noContext", parent.getName());
      assertTrue(parent.isProject());
    });
  }
}
