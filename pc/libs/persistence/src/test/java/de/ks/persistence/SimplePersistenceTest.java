package de.ks.persistence;

/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import org.junit.Before;
import org.junit.Test;

import javax.persistence.criteria.CriteriaQuery;

import static org.junit.Assert.assertEquals;

/**
 *
 */
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
