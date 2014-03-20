/*
 * Copyright [2014] [Christian Loehnert]
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

/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Currently used to initialize persistence and to provide access to the {@link EntityManager}
 */
public class EntityManagerProvider {
  private static final String PU_KEY = "de.ks.persistence.persistenceunit";
  private static final Logger log = LoggerFactory.getLogger(EntityManagerProvider.class);
  public static String PERSISTENCE_UNIT_NAME = "persistence";
  private static final ReentrantLock lock = new ReentrantLock();
  private static EntityManagerFactory entityManagerFactory;

  static {
    new Thread(() -> initialize()).start();
  }

  private static void initialize() {
    readPersistenceUnitFromProperties();

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

  private static void readPersistenceUnitFromProperties() {
    String propertyPath = "/de/ks/persistence/persistence.properties";
    Properties properties = new Properties();
    try {
      InputStream resource = EntityManagerProvider.class.getResourceAsStream(propertyPath);
      if (resource == null) {
        log.warn("Could not find {}", propertyPath);
        return;
      }
      properties.load(resource);
      String persistenceUnitName = properties.getProperty(PU_KEY);
      if (persistenceUnitName != null) {
        log.info("Using PersistenceUnit from property file. PU={}", persistenceUnitName);
        PERSISTENCE_UNIT_NAME = persistenceUnitName;
      } else {
        log.warn("No PersistenceUnit defined in property file {}", propertyPath);
      }
    } catch (IOException e) {
      log.warn("Could not load {}", propertyPath);
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
