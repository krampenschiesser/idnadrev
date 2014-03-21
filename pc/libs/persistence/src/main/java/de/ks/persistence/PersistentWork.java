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

import javax.persistence.EntityManager;
import java.util.function.Consumer;

/**
 * Handles transactions and {@link EntityManager} closing.
 * Usually used as an anonymous class.
 */
public class PersistentWork {
  private static final Logger log = LoggerFactory.getLogger(PersistentWork.class);

  protected EntityManager em;
  protected Consumer<EntityManager> consumer;


  public PersistentWork(Consumer<EntityManager> consumer) {
    this.consumer = consumer;
    em = EntityManagerProvider.getEntityManager();
    run();
  }

  public PersistentWork() {
    em = EntityManagerProvider.getEntityManager();
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

  protected void execute() {
    if (consumer != null) {
      consumer.accept(em);
    }
  }
}
