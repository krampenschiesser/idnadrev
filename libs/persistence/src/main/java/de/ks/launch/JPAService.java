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
package de.ks.launch;

import de.ks.persistence.EntityManagerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JPAService extends Service {
  private static final Logger log = LoggerFactory.getLogger(JPAService.class);
  private static final String PU_KEY = "de.ks.persistence.persistenceunit";
  public static String PERSISTENCE_UNIT_NAME = "persistence";

  protected EntityManagerFactory factory;

  @Override
  protected void doStart() {
    readPersistenceUnitFromProperties();
    if (factory == null) {
      factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    }
  }

  @Override
  protected void doStop() {
    if (factory != null) {
      factory.close();
    }
  }

  private static void readPersistenceUnitFromProperties() {
    String propertyPath = "/de/ks/persistence/persistence.properties";
    Properties properties = new Properties();
    try {
      InputStream resource = EntityManagerProvider.class.getResourceAsStream(propertyPath);
      if (resource == null) {
        log.warn("Could not find {} will use default PersistenceUnit={}", propertyPath, PERSISTENCE_UNIT_NAME);
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

  public EntityManagerFactory getEntityManagerFactory() {
    return factory;
  }
}
