/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Handles transactions and {@link EntityManager} closing.
 * Usually used as an anonymous class.
 */
public class PersistentWork {
  private static final Logger log = LoggerFactory.getLogger(PersistentWork.class);

  public static void run(Consumer<EntityManager> consumer) {
    new PersistentWork(consumer).run();
  }

  public static <T> T read(Function<EntityManager, T> function) {
    return new PersistentWork(function).run();
  }

  public static <T> void persist(T t) {
    run((em) -> em.persist(t));
  }

  public static void deleteAllOf(Class<?> clazz) {
    run((em) -> em.createQuery("delete from " + clazz.getName()).executeUpdate());
  }

  public static <T> List<T> from(Class<T> clazz) {
    return from(clazz, null);
  }

  public static <T> List<T> from(Class<T> clazz, BiConsumer<Root<T>, CriteriaQuery<T>> consumer) {
    return read((em) -> {
      CriteriaQuery<T> query = (CriteriaQuery<T>) em.getCriteriaBuilder().createQuery(clazz);

      Root<T> root = query.from(clazz);
      query.select(root);

      if (consumer != null) {
        consumer.accept(root, query);
      }

      return em.createQuery(query).getResultList();
    });
  }

  protected EntityManager em;
  protected Consumer<EntityManager> consumer;
  protected Function<EntityManager, ?> function;

  private PersistentWork() {
    em = CDI.current().select(EntityManager.class).get();
  }

  private PersistentWork(Consumer<EntityManager> consumer) {
    this();
    this.consumer = consumer;
  }

  private PersistentWork(Function<EntityManager, ?> function) {
    this();
    this.function = function;
  }

  @SuppressWarnings("unchecked")
  protected <T> T run() {
    if (!em.getTransaction().isActive()) {
      em.clear();
      em.getTransaction().begin();
      try {
        Object retval = execute();
        em.getTransaction().commit();
        return (T) retval;
      } catch (Exception e) {
        log.error("Error occured during commit phase:", e);
        em.getTransaction().rollback();
        throw e;
      } finally {
        em.clear();
        em.close();
      }
    }
    return null;
  }

  protected Object execute() {
    if (consumer != null) {
      consumer.accept(em);
      return null;
    } else if (function != null) {
      return function.apply(em);
    }
    return null;
  }
}
