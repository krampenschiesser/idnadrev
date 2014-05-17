/*
 * Copyright [${YEAR}] [Christian Loehnert]
 *
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

package de.ks.beagle.entity;


import de.ks.CDIRunner;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.entity.AbstractPersistentObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 *
 */
@RunWith(CDIRunner.class)
public class TaskTest {

  @Before
  public void setUp() throws Exception {
    PersistentWork.run((em) -> em.createNativeQuery("delete from " + Note.NOTE_TAG_JOINTABLE).executeUpdate());

    List<Class<? extends AbstractPersistentObject<?>>> entitiesToDelete = Arrays.asList(//
            Tag.class, WorkUnit.class, NoteFile.class, Note.class, Task.class, Context.class);
    for (Class<? extends AbstractPersistentObject<?>> clazz : entitiesToDelete) {
      PersistentWork.deleteAllOf(clazz);
    }
  }

  @Test
  public void testPersist() throws Exception {
    Task task = new Task("bla");
    persistTask(task);
    assertNotNull(task.getCreationTime());
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

    persistTask(task);
    PersistentWork.run((em) -> {
      Task readTask = em.find(Task.class, task.getId());
      assertEquals(workUnitAmount, readTask.getWorkUnits().size());
      assertEquals(3, task.getSpentMinutes());
    });
  }

  @Test
  public void testNotes() throws Exception {
    final String fileName = "img.jpg";
    final String tagName = "bla";

    Note note = new Note("a note");
    note.addFile(new File(getClass().getResource(fileName).toURI()));
    Task task = new Task("test");
    task.addNote(note);
    Tag tag = new Tag(tagName);
    note.addTag(tag);

    persistTask(task);

    PersistentWork.run((em) -> {
      Task readTask = em.find(Task.class, task.getId());
      assertEquals(1, readTask.getNotes().size());
      Note readNote = readTask.getNotes().iterator().next();
      assertEquals(1, readNote.getFiles().size());
      assertEquals(fileName, readNote.getFiles().iterator().next().getName());
      assertEquals(1, readNote.getTags().size());
      assertEquals(tagName, readNote.getTags().iterator().next().getName());
    });

    PersistentWork.run((em) -> {
      Note readNote = em.find(Note.class, note.getId());
      em.remove(readNote);
    });
    PersistentWork.run((em) -> {
      Tag readTag = em.find(Tag.class, tag.getId());
      assertNotNull(readTag);
      NoteFile readFile = em.find(NoteFile.class, note.getFiles().iterator().next().getId());
      assertNull(readFile);
    });
    PersistentWork.run((em) -> {
      em.remove(em.find(Task.class, task.getId()));
    });
  }

  @Test
  public void testContext() throws Exception {
    String contextName = "home";
    Task task = new Task("blubber");
    task.setContext(new Context(contextName));

    persistTask(task);

    PersistentWork.run((em) -> {
      Task readTask = em.find(Task.class, task.getId());
      assertNotNull(readTask.getContext());
      assertEquals(contextName, readTask.getContext().getName());
    });
    PersistentWork.run((em) -> {
      em.remove(em.find(Task.class, task.getId()));
    });
    PersistentWork.run((em) -> {
      Context context = em.find(Context.class, task.getContext().getId());
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

    persistTask(parent);

    PersistentWork.run((em) -> {
      Task readParent = em.find(parent.getClass(), parent.getId());
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

    PersistentWork.run((em) -> {
      em.remove(em.find(parent.getClass(), parent.getId()));
    });

    PersistentWork.run((em) -> {
      List foundTasks = em.createQuery("from " + Task.class.getName()).getResultList();
      assertEquals(0, foundTasks.size());
    });
  }

  private void persistTask(final Task task) {
    PersistentWork.run((em) -> {
      em.persist(task);
    });
  }


}
