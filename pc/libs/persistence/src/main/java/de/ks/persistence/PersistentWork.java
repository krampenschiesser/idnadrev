package de.ks.persistence;

/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

/**
 * Handles transactions and {@link EntityManager} closing.
 * Usually used as an anonymous class.
 * return values might be set with {@link #setReturnValue(Object)}
 * and read with {@link #get()}
 */
public abstract class PersistentWork {
  private static final Logger log = LogManager.getLogger(PersistentWork.class);

  protected EntityManager em;
  protected CriteriaBuilder builder;
  private Object value;

  public PersistentWork() {
    em = EntityManagerProvider.getEntityManager();
    builder = em.getCriteriaBuilder();
    run();
  }

  protected void run() {
    if (!em.getTransaction().isActive()) {
      em.clear();
      em.getTransaction().begin();
      try {
        execute();
        em.getTransaction().commit();
      } catch (Exception e) {
        log.error("Error occured during commit phase:", e);
        em.getTransaction().rollback();
        throw e;
      } finally {
        em.clear();
        em.close();
      }
    }
  }

  protected abstract void execute();

  protected void setReturnValue(Object value) {
    this.value = value;
  }

  @SuppressWarnings("unchecked")
  public <T> T get() {
    return (T) value;
  }
}
