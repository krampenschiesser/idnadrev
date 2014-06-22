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

import com.google.common.io.Files;
import de.ks.LauncherRunner;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.entity.AbstractPersistentObject;
import de.ks.persistence.entity.NamedPersistentObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 */
@RunWith(LauncherRunner.class)
public class PersistEntitiesTest {
  private List<Class<? extends AbstractPersistentObject<?>>> allEntityClasses = Arrays.asList(Category.class, Context.class, File.class, Note.class, Tag.class, Thought.class, WorkUnit.class, Task.class);

  private List<NamedPersistentObject> simpleEntities = new ArrayList<NamedPersistentObject>() {{
    add(new Category("myCategory"));
    add(new Context("myContext"));
    add(new Tag("myTag"));
    add(new Thought("myThought"));
  }};

  @Before
  public void setUp() throws Exception {
    for (Class<?> clazz : allEntityClasses) {
      PersistentWork.deleteAllOf(clazz);
    }
  }

  @Test
  public void testSimpleEntities() throws Exception {
    simpleEntities.forEach(entity -> PersistentWork.persist(entity));
    //read em
    simpleEntities.forEach(entity -> PersistentWork.run(em -> {
      Class<? extends NamedPersistentObject> entityClass = entity.getClass();
      CriteriaBuilder builder = em.getCriteriaBuilder();
      CriteriaQuery<? extends NamedPersistentObject> query = builder.createQuery(entityClass);
      query.from(entityClass);

      List<? extends NamedPersistentObject> found = em.createQuery(query).getResultList();
      assertEquals(1, found.size());
      assertEquals(entity, found.get(0));
    }));
  }

  @Test(expected = PersistenceException.class)
  public void testNoDuplicateNamedPersistentObject() throws Exception {
    PersistentWork.run((em) -> {
      em.persist(new Tag("hello"));
      em.persist(new Tag("hello"));
    });
  }

  @Test
  public void testPersistNote() throws Exception {
    java.io.File imageFile = new java.io.File(getClass().getResource("img.jpg").toURI());
    assertTrue(imageFile.exists());
    byte[] data = Files.toByteArray(imageFile);

    Note note = new Note("myNote");


    File noteFile = new File(imageFile.getName());
    noteFile.setData(data);
    note.addFile(noteFile);

    PersistentWork.run((em) -> {
      em.persist(note);
      em.persist(noteFile);
    });

    PersistentWork.run((em) -> {
      Note readNote = em.find(Note.class, note.getId());
      assertEquals(note, readNote);
      assertNotNull(note.getFiles());
      assertEquals(1, note.getFiles().size());
      File readNoteFile = note.getFiles().iterator().next();
      assertEquals(data.length, readNoteFile.getData().length);
      assertEquals(data, readNoteFile.getData());
    });
  }

  @Test
  public void testPersistNoteDirectFileAdding() throws Exception {
    java.io.File imageFile = new java.io.File(getClass().getResource("img.jpg").toURI());
    assertTrue(imageFile.exists());

    Note note = new Note("myNote");
    note.addFile(imageFile);

    PersistentWork.persist(note);

    PersistentWork.run((em) -> {
      Note readNote = em.find(Note.class, note.getId());
      assertEquals(note, readNote);
      assertNotNull(note.getFiles());
      assertEquals(1, note.getFiles().size());
      File readNoteFile = note.getFiles().iterator().next();
      assertEquals(imageFile.length(), readNoteFile.getData().length);
    });
  }

  @Test
  public void testPersistThoughtDirectFileAdding() throws Exception {
    java.io.File imageFile = new java.io.File(getClass().getResource("img.jpg").toURI());
    assertTrue(imageFile.exists());

    Thought thought = new Thought("myThought");
    thought.addFile(imageFile);

    PersistentWork.persist(thought);

    PersistentWork.run((em) -> {
      Thought readNote = em.find(Thought.class, thought.getId());
      assertEquals(thought, readNote);
      assertNotNull(thought.getFiles());
      assertEquals(1, thought.getFiles().size());
      File readNoteFile = thought.getFiles().iterator().next();
      assertEquals(imageFile.length(), readNoteFile.getData().length);
    });
  }
}
