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
package de.ks.persistence;

import de.ks.launch.JPAService;
import de.ks.version.VersionUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public abstract class PersistentVersionUpgrade implements VersionUpgrade {
  private static final Logger log = LoggerFactory.getLogger(PersistentVersionUpgrade.class);
  private final String driver;
  private final String user;
  private final String url;
  private final String pw;

  public PersistentVersionUpgrade(String password) {
    pw = password;
    JPAService jpaService = new JPAService();
    jpaService.start();
    EntityManagerFactory entityManagerFactory = jpaService.getEntityManagerFactory();
    Map<String, Object> properties = entityManagerFactory.getProperties();
    driver = (String) properties.get("javax.persistence.jdbc.driver");
    user = (String) properties.get("javax.persistence.jdbc.user");
    url = (String) properties.get("javax.persistence.jdbc.url");
  }

  protected void executeStatement(String sqlString) {
    Connection dbConnection = null;
    Statement statement = null;

    try {
      dbConnection = getDBConnection();
      statement = dbConnection.createStatement();

      statement.execute(sqlString);
      log.info("Successfully executed sql statement {}", sqlString);
    } catch (SQLException e) {
      log.error("Could not executed statement {}", sqlString, e);
      throw new RuntimeException(e);
    } finally {
      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
      if (dbConnection != null) {
        try {
          dbConnection.close();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }

      }
    }
  }

  private Connection getDBConnection() {
    try {
      Class.forName(driver);
    } catch (ClassNotFoundException e) {
      log.error("Could not find the given jdbc driver {}", driver, e);
    }

    try {
      Connection dbConnection = null;
      dbConnection = DriverManager.getConnection(url, user, pw);
      return dbConnection;
    } catch (SQLException e) {
      log.error("Could not open connection {} for user {}", url, user);
      throw new RuntimeException(e);
    }
  }

}
