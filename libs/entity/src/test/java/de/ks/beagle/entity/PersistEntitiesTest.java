package de.ks.beagle.entity;/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.io.Files;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.entity.AbstractPersistentObject;
import de.ks.persistence.entity.NamedPersistentObject;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaQuery;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 */
public class PersistEntitiesTest {
  private List<Class<? extends AbstractPersistentObject<?>>> allEntityClasses = Arrays.asList(Category.class, Context.class, NoteFile.class, Note.class, Tag.class, Thought.class, WorkUnit.class, Task.class, WorkType.class);

  private List<NamedPersistentObject> simpleEntities = new ArrayList<NamedPersistentObject>() {{
    add(new Category("myCategory"));
    add(new Context("myContext"));
    add(new Tag("myTag"));
    add(new Thought("myThought"));
    add(new WorkType("myWork"));
  }};

  @Before
  public void setUp() throws Exception {
    for (Class<?> clazz : allEntityClasses) {
      new PersistentWork() {
        @Override
        protected void execute() {
          em.createQuery("delete from " + clazz.getName()).executeUpdate();
        }
      };
    }

  }

  @Test
  public void testSimpleEntities() throws Exception {
    //save em
    for (NamedPersistentObject<?> entity : simpleEntities) {
      new PersistentWork() {
        @Override
        protected void execute() {
          em.persist(entity);
        }
      };
    }
    //read em
    for (NamedPersistentObject entity : simpleEntities) {
      new PersistentWork() {
        @Override
        protected void execute() {
          Class<? extends NamedPersistentObject> entityClass = entity.getClass();
          CriteriaQuery<? extends NamedPersistentObject> query = builder.createQuery(entityClass);
          query.from(entityClass);

          List<? extends NamedPersistentObject> found = em.createQuery(query).getResultList();
          assertEquals(1, found.size());
          assertEquals(entity, found.get(0));
        }
      };
    }
  }

  @Test(expected = PersistenceException.class)
  public void testNoDuplicateNamedPersistentObject() throws Exception {
    new PersistentWork() {
      @Override
      protected void execute() {
        em.persist(new Tag("hello"));
        em.persist(new Tag("hello"));
      }
    };
  }

  @Test
  public void testPersistNote() throws Exception {
    File imageFile = new File(getClass().getResource("img.jpg").toURI());
    assertTrue(imageFile.exists());
    byte[] data = Files.toByteArray(imageFile);

    Note note = new Note("myNote");


    NoteFile noteFile = new NoteFile(imageFile.getName());
    noteFile.setData(data);
    note.addNoteFile(noteFile);

    new PersistentWork() {
      @Override
      protected void execute() {
        em.persist(note);
        em.persist(noteFile);
      }
    };

    new PersistentWork() {
      @Override
      protected void execute() {
        Note readNote = em.find(Note.class, note.getId());
        assertEquals(note, readNote);
        assertNotNull(note.getFiles());
        assertEquals(1, note.getFiles().size());
        NoteFile readNoteFile = note.getFiles().iterator().next();
        assertEquals(data.length, readNoteFile.getData().length);
        assertEquals(data, readNoteFile.getData());
      }
    };
  }

  @Test
  public void testPersistNoteDirectFileAdding() throws Exception {
    File imageFile = new File(getClass().getResource("img.jpg").toURI());
    assertTrue(imageFile.exists());

    Note note = new Note("myNote");
    note.addFile(imageFile);

    new PersistentWork() {
      @Override
      protected void execute() {
        em.persist(note);
      }
    };

    new PersistentWork() {
      @Override
      protected void execute() {
        Note readNote = em.find(Note.class, note.getId());
        assertEquals(note, readNote);
        assertNotNull(note.getFiles());
        assertEquals(1, note.getFiles().size());
        NoteFile readNoteFile = note.getFiles().iterator().next();
        assertEquals(imageFile.length(), readNoteFile.getData().length);
      }
    };
  }
}
