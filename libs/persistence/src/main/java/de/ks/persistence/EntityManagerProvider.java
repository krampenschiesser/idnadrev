package de.ks.persistence;

/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import java.net.URL;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Currently used to initialize persistence and to provide access to the {@link EntityManager}
 */
public class EntityManagerProvider {
  public static final String PERSISTENCE_UNIT_NAME = "persistence";
  protected URL hibernateCfg = EntityManagerProvider.class.getResource("/META-INF/persistence.xml");
  private static final ReentrantLock lock = new ReentrantLock();
  private static EntityManagerFactory entityManagerFactory;

  static {
    new Thread(() -> initialize()).start();
  }

  private static void initialize() {
    if (entityManagerFactory != null) {
      return;
    }
    lock.lock();
    try {
      if (entityManagerFactory != null) {
        return;
      } else {
        entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
      }
    } finally {

      lock.unlock();
    }
  }


  public static EntityManagerFactory getEntityManagerFactory() {
    if (entityManagerFactory == null) {
      initialize();
    }
    return entityManagerFactory;
  }

  public static EntityManager getEntityManager() {
    return getEntityManagerFactory().createEntityManager();
  }

  public static CriteriaBuilder getBuilder() {
    return entityManagerFactory.getCriteriaBuilder();
  }
}
