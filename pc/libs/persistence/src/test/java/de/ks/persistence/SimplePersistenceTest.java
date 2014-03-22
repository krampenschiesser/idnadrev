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

package de.ks.persistence;


import de.ks.CDIRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.criteria.CriteriaQuery;

import static org.junit.Assert.assertEquals;

/**
 *
 */
@RunWith(CDIRunner.class)
public class SimplePersistenceTest {
  @Before
  public void setUp() throws Exception {
    new PersistentWork() {
      @Override
      protected void execute() {
        em.createQuery("delete from " + DummyEntity.class.getName()).executeUpdate();
      }
    };
  }

  @Test
  public void testPersist() throws Exception {
    new PersistentWork() {
      @Override
      protected void execute() {
        DummyEntity entity = new DummyEntity("Hello World");
        em.persist(entity);
      }
    };
    //read
    new PersistentWork() {
      @Override
      protected void execute() {
        CriteriaQuery<DummyEntity> query = em.getCriteriaBuilder().createQuery(DummyEntity.class);
        query.from(DummyEntity.class);
        DummyEntity readEntity = em.createQuery(query).getSingleResult();

        assertEquals("Hello World", readEntity.getName());
      }
    };
  }

  @Test
  public void testTransactionFailed() {
    try {
      new PersistentWork() {
        @Override
        protected void execute() {
          DummyEntity entity = new DummyEntity("Hello World");
          em.persist(entity);
          throw new RuntimeException();
        }
      };
    } catch (RuntimeException e) {
      //ok!
    }

    new PersistentWork() {
      @Override
      protected void execute() {
        CriteriaQuery<DummyEntity> query = em.getCriteriaBuilder().createQuery(DummyEntity.class);
        query.from(DummyEntity.class);

        int amount = em.createQuery(query).getResultList().size();
        assertEquals(0, amount);
      }
    };
  }
}
