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

package de.ks.idnadrev.entity;

import de.ks.flatjsondb.PersistentWork;
import de.ks.idnadrev.entity.information.Information;
import de.ks.idnadrev.entity.information.TextInfo;
import de.ks.standbein.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.Assert.*;

/**
 *
 */
public class TaskTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IntegrationTestModule());
  @Inject
  PersistentWork persistentWork;

  @Test
  public void testPersist() throws Exception {
    Task task = new Task("bla");
    persistentWork.persist(task);
    assertNotNull(task.getCreationTime());
    Task reload = persistentWork.reload(task);
    assertNotSame(task, reload);
    assertNotNull(reload.getOutcome());
  }

  @Test
  public void testWorkUnits() throws Exception {
    int workUnitAmount = 3;

    Task task = new Task("Mach Sauber!");
    for (int i = 0; i < workUnitAmount; i++) {
      task.start();
      Thread.sleep(100);
      task.stop();
    }
    WorkUnit last = task.getWorkUnits().last();
    LocalDateTime end = last.getStart().plusMinutes(3);
    last.setEnd(end);

    persistentWork.persist(task);
    persistentWork.run((em) -> {
      Task readTask = em.findById(Task.class, task.getId());
      assertEquals(workUnitAmount, readTask.getWorkUnits().size());
      assertEquals(3, task.getSpentMinutes());
    });
  }

  @Test
  public void testNotes() throws Exception {
    final String fileName = "img.jpg";
    final String tagName = "bla";

    TextInfo information = new TextInfo("a note");
    Task task = new Task("test");
    Tag tag = new Tag(tagName);
    information.addTag(tag);
    task.addInformation(information);
//    information.setTask(task);

    persistentWork.persist(task, tag, information);

    persistentWork.run((em) -> {
      Task readTask = em.findById(Task.class, task.getId());

      assertEquals(1, readTask.getInformation().size());
      Information<?> readInformation = readTask.getInformation().iterator().next();
      assertEquals(1, readInformation.getTags().size());
      assertEquals(tagName, readInformation.getTags().iterator().next().getDisplayName());
    });

    persistentWork.run((em) -> {
      TextInfo readInformation = em.findById(TextInfo.class, information.getId());
      em.remove(readInformation);
    });
    persistentWork.run((em) -> {
      Tag readTag = em.findById(Tag.class, tag.getId());
      assertNotNull(readTag);
    });
    persistentWork.run((em) -> {
      em.remove(em.findById(Task.class, task.getId()));
    });
  }

  @Test
  public void testContext() throws Exception {
    String contextName = "home";
    Task task = new Task("blubber");
    task.setContext(new Context(contextName));

    persistentWork.persist(task);

    persistentWork.run((em) -> {
      Task readTask = em.findById(Task.class, task.getId());
      assertNotNull(readTask.getContext());
      assertEquals(contextName, readTask.getContext().getName());
    });
    persistentWork.run((em) -> {
      em.remove(em.findById(Task.class, task.getId()));
    });
    persistentWork.run((em) -> {
      Context context = em.findById(Context.class, task.getContext().getId());
      assertNotNull(context);
      assertEquals(contextName, context.getName());
    });
  }

  @Test
  public void testProjectStructure() throws Exception {
    Task parent = new Task("Parent");
    parent.addChild(new Task("Child1"));
    Task child2 = new Task("Child2");
    parent.addChild(child2);
    child2.addChild(new Task("SubChild"));

    persistentWork.persist(parent);

    persistentWork.run((em) -> {
      Task readParent = persistentWork.reload(parent);
      Set<Task> children = readParent.getChildren();
      assertEquals(2, children.size());
      for (Task child : children) {
        if (child.getName().endsWith("2")) {
          assertEquals(1, child.getChildren().size());
        } else {
          assertEquals(0, child.getChildren().size());
        }
      }
    });

    persistentWork.run((em) -> {
      em.remove(persistentWork.reload(parent));
    });

    assertEquals(0, persistentWork.from(Task.class).size());
  }

  @Test
  public void testFinish() throws Exception {
    Task task = new Task("bla");
    task.setState(TaskState.LATER);
    persistentWork.persist(task);

    persistentWork.run(session -> {
      persistentWork.reload(task).setFinished(true);
    });

    persistentWork.run(session -> {
      Task reload = persistentWork.reload(task);
      assertEquals(TaskState.NONE, reload.getState());
      assertTrue(reload.isFinished());
      assertNotNull(reload.getFinishTime());
    });
  }
}
