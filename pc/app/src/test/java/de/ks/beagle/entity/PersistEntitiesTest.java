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

import com.google.common.io.Files;
import de.ks.CDIRunner;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.entity.AbstractPersistentObject;
import de.ks.persistence.entity.NamedPersistentObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 */
@RunWith(CDIRunner.class)
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
    simpleEntities.forEach(entity -> new PersistentWork(em -> em.persist(entity)));
    //read em
    simpleEntities.forEach(entity -> new PersistentWork(em -> {
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
    new PersistentWork((em) -> {
      em.persist(new Tag("hello"));
      em.persist(new Tag("hello"));
    });
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

    new PersistentWork((em) -> {
      em.persist(note);
      em.persist(noteFile);
    });

    new PersistentWork((em) -> {
      Note readNote = em.find(Note.class, note.getId());
      assertEquals(note, readNote);
      assertNotNull(note.getFiles());
      assertEquals(1, note.getFiles().size());
      NoteFile readNoteFile = note.getFiles().iterator().next();
      assertEquals(data.length, readNoteFile.getData().length);
      assertEquals(data, readNoteFile.getData());
    });
  }

  @Test
  public void testPersistNoteDirectFileAdding() throws Exception {
    File imageFile = new File(getClass().getResource("img.jpg").toURI());
    assertTrue(imageFile.exists());

    Note note = new Note("myNote");
    note.addFile(imageFile);

    new PersistentWork(em -> em.persist(note));


    new PersistentWork((em) -> {
        Note readNote = em.find(Note.class, note.getId());
        assertEquals(note, readNote);
        assertNotNull(note.getFiles());
        assertEquals(1, note.getFiles().size());
        NoteFile readNoteFile = note.getFiles().iterator().next();
        assertEquals(imageFile.length(), readNoteFile.getData().length);
    });
  }
}
