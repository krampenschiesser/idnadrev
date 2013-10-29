package de.ks.beagle.entity;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.persistence.PersistentWork;
import de.ks.persistence.entity.AbstractPersistentObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 *
 */
public class TaskTest {
  @Before
  public void setUp() throws Exception {
    new PersistentWork() {
      @Override
      protected void execute() {
        em.createNativeQuery("delete from " + Note.NOTE_TAG_JOINTABLE).executeUpdate();
      }
    };
    new PersistentWork() {
      @Override
      protected void execute() {
        List<Class<? extends AbstractPersistentObject<?>>> entitiesToDelete = Arrays.asList(//
                Tag.class, WorkUnit.class, NoteFile.class, Note.class, Task.class, Context.class);
        for (Class<? extends AbstractPersistentObject<?>> clazz : entitiesToDelete) {
          em.createQuery("delete from " + clazz.getName()).executeUpdate();
        }
      }
    };
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
    new PersistentWork() {
      @Override
      protected void execute() {
        Task readTask = em.find(Task.class, task.getId());
        assertEquals(workUnitAmount, readTask.getWorkUnits().size());
        assertEquals(3, task.getSpentMinutes());
      }
    };
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

    new PersistentWork() {
      @Override
      protected void execute() {
        Task readTask = em.find(Task.class, task.getId());
        assertEquals(1, readTask.getNotes().size());
        Note readNote = readTask.getNotes().iterator().next();
        assertEquals(1, readNote.getFiles().size());
        assertEquals(fileName, readNote.getFiles().iterator().next().getName());
        assertEquals(1, readNote.getTags().size());
        assertEquals(tagName, readNote.getTags().iterator().next().getName());
      }
    };

    new PersistentWork() {
      @Override
      protected void execute() {
        Note readNote = em.find(Note.class, note.getId());
        em.remove(readNote);
      }
    };
    new PersistentWork() {
      @Override
      protected void execute() {
        Tag readTag = em.find(Tag.class, tag.getId());
        assertNotNull(readTag);
        NoteFile readFile = em.find(NoteFile.class, note.getFiles().iterator().next().getId());
        assertNull(readFile);
      }
    };
    new PersistentWork() {
      @Override
      protected void execute() {
        em.remove(em.find(Task.class, task.getId()));
      }
    };
  }

  @Test
  public void testContext() throws Exception {
    String contextName = "home";
    Task task = new Task("blubber");
    task.setContext(new Context(contextName));

    persistTask(task);

    new PersistentWork() {
      @Override
      protected void execute() {
        Task readTask = em.find(Task.class, task.getId());
        assertNotNull(readTask.getContext());
        assertEquals(contextName, readTask.getContext().getName());
      }
    };
    new PersistentWork() {
      @Override
      protected void execute() {
        em.remove(em.find(Task.class, task.getId()));
      }
    };
    new PersistentWork() {
      @Override
      protected void execute() {
        Context context = em.find(Context.class, task.getContext().getId());
        assertNotNull(context);
        assertEquals(contextName, context.getName());
      }
    };
  }

  @Test
  public void testProjectStructure() throws Exception {
    Task parent = new Task("Parent");
    parent.addChild(new Task("Child1"));
    Task child2 = new Task("Child2");
    parent.addChild(child2);
    child2.addChild(new Task("SubChild"));

    persistTask(parent);

    new PersistentWork() {
      @Override
      protected void execute() {
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
      }
    };

    new PersistentWork() {
      @Override
      protected void execute() {
        em.remove(em.find(parent.getClass(), parent.getId()));
      }
    };

    new PersistentWork() {
      @Override
      protected void execute() {
        List foundTasks = em.createQuery("from " + Task.class.getName()).getResultList();
        assertEquals(0, foundTasks.size());
      }
    };
  }

  private void persistTask(final Task task) {
    new PersistentWork() {
      @Override
      protected void execute() {
        em.persist(task);
      }
    };
  }


}
