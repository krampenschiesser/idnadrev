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
package de.ks.persistence.connection;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.hibernate.jpa.AvailableSettings;
import org.hibernate.service.spi.Configurable;
import org.hibernate.service.spi.Startable;
import org.hibernate.service.spi.Stoppable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class ConnectionProvider implements org.hibernate.engine.jdbc.connections.spi.ConnectionProvider, Configurable, Startable, Stoppable {
  private static final Logger log = LoggerFactory.getLogger(ConnectionProvider.class);
  private PoolProperties properties;
  private DataSource datasource;

  @Override
  public Connection getConnection() throws SQLException {
    return datasource.getConnection();
  }

  @Override
  public void closeConnection(Connection conn) throws SQLException {
    conn.close();
  }

  @Override
  public boolean supportsAggressiveRelease() {
    return false;
  }

  @Override
  public boolean isUnwrappableAs(Class unwrapType) {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> unwrapType) {
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void configure(Map configurationValues) {
    properties = createProperties(configurationValues);
  }

  protected PoolProperties createProperties(Map<String, String> configurationValues) {
    String jdbcUrl = (String) configurationValues.get(AvailableSettings.JDBC_URL);
    String driver = (String) configurationValues.get(AvailableSettings.JDBC_DRIVER);
    String user = (String) configurationValues.get(AvailableSettings.JDBC_USER);
    String pw = (String) configurationValues.get(AvailableSettings.JDBC_PASSWORD);
    log.info("Configuring pool with\n\tjdcburl={}\n\tdriver={}", jdbcUrl, driver);

    PoolProperties p = new PoolProperties();
    p.setUrl(jdbcUrl);
    p.setDriverClassName(driver);
    p.setUsername(user);
    p.setPassword(pw);
    p.setJmxEnabled(false);
    p.setTestWhileIdle(false);
    p.setTestOnBorrow(true);
    p.setValidationQuery("SELECT 1");
    p.setTestOnReturn(false);
    p.setValidationInterval(30000);
    p.setTimeBetweenEvictionRunsMillis(30000);
    p.setMaxActive(100);
    p.setInitialSize(10);
    p.setMaxWait(10000);
    p.setRemoveAbandonedTimeout(60);
    p.setMinEvictableIdleTimeMillis(30000);
    p.setMinIdle(10);
    p.setLogAbandoned(true);
    p.setRemoveAbandoned(true);
    p.setDefaultAutoCommit(false);
    p.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;" + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
    return p;
  }

  @Override
  public void start() {
    Profiler profiler = new Profiler("profiler");
    datasource = new DataSource();
    datasource.setPoolProperties(properties);
    profiler.stop();
    log.info("Started {} in {}ms", getClass().getSimpleName(), profiler.elapsedTime() / 1000 / 1000);
  }

  @Override
  public void stop() {
    Profiler profiler = new Profiler("profiler");
    datasource = new DataSource();
    datasource.setPoolProperties(properties);
    profiler.stop();
    log.info("Stopped {} in {}ms", getClass().getSimpleName(), profiler.elapsedTime() / 1000 / 1000);
  }
}
