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

import static org.junit.Assert.assertEquals;

/**
 *
 */
@RunWith(LauncherRunner.class)
public class PersistEntitiesTest {
  private List<Class<? extends AbstractPersistentObject<?>>> allEntityClasses = Arrays.asList(Category.class, Context.class, FileReference.class, Note.class, Tag.class, Thought.class, WorkUnit.class, Task.class);

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
    Note note = new Note("myNote");

    PersistentWork.run((em) -> {
      em.persist(note);
    });

    PersistentWork.run((em) -> {
      Note readNote = em.find(Note.class, note.getId());
      assertEquals(note, readNote);
    });
  }

}
