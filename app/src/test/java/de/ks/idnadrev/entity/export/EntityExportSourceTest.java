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

package de.ks.idnadrev.entity.export;

import de.ks.LauncherRunner;
import de.ks.idnadrev.entity.Thought;
import de.ks.persistence.PersistentWork;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class EntityExportSourceTest {
  @Before
  public void setUp() throws Exception {
    PersistentWork.deleteAllOf(Thought.class);

    PersistentWork.run(em -> {
      for (int i = 0; i < 342; i++) {
        em.persist(new Thought(String.format("%02d", i)));
      }
    });
  }

  @Test
  public void testExportSource() throws Exception {
    List<Long> allIds = getAllIds();

    EntityExportSource<Thought> source = new EntityExportSource<>(allIds, Thought.class);
    HashSet<Thought> thoughtSet = new HashSet<>();
    List<Thought> thoughtList = new LinkedList<>();
    EntityExportIterator<Thought> iterator = (EntityExportIterator<Thought>) source.iterator();
    for (; iterator.hasNext(); ) {
      Thought next = iterator.next();
      assertNotNull(next);
      assertNotNull(next.getName());
      thoughtSet.add(next);
      thoughtList.add(next);
    }
    assertEquals(342, thoughtSet.size());
    assertEquals(thoughtList.size(), thoughtSet.size());

    assertFalse(iterator.em.isOpen());
  }

  @Test
  public void test300() throws Exception {
    List<Long> ids = getIds(300);
    EntityExportSource<Thought> source = new EntityExportSource<>(ids, Thought.class);

    HashSet<Thought> thoughtSet = new HashSet<>();
    List<Thought> thoughtList = new LinkedList<>();
    for (Thought thought : source) {
      thoughtSet.add(thought);
      thoughtList.add(thought);
    }
    assertEquals(300, thoughtSet.size());
    assertEquals(thoughtList.size(), thoughtSet.size());
  }

  private static final Logger log = LoggerFactory.getLogger(EntityExportSourceTest.class);

  protected List<Long> getAllIds() {
    return PersistentWork.read(em -> {
      CriteriaQuery<Long> criteriaQuery = em.getCriteriaBuilder().createQuery(Long.class);
      Root<Thought> root = criteriaQuery.from(Thought.class);
      Path<Long> id = root.<Long>get("id");
      criteriaQuery.select(id);
      return em.createQuery(criteriaQuery).getResultList();
    });
  }

  protected List<Long> getIds(int maxResults) {
    return PersistentWork.read(em -> {
      CriteriaQuery<Long> criteriaQuery = em.getCriteriaBuilder().createQuery(Long.class);
      Root<Thought> root = criteriaQuery.from(Thought.class);
      Path<Long> id = root.<Long>get("id");
      criteriaQuery.select(id);
      TypedQuery<Long> query = em.createQuery(criteriaQuery);
      query.setMaxResults(maxResults);
      return query.getResultList();
    });
  }
}