/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.version;

import de.ks.persistence.PersistentVersionUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Upgrade051 extends PersistentVersionUpgrade {
  private static final Logger log = LoggerFactory.getLogger(Upgrade051.class);

  public Upgrade051() {
    super("");
  }

  @Override
  public int getVersion() {
    return 51;
  }

  @Override
  public void performUpgrade() {
    log.info("Adding booking tables for version upgrade from 0.5.0 to 0.5.1");

    String bookingPatternTable = "CREATE TABLE PUBLIC.BOOKINGPATTERN(\n" +
      "id bigint(19) auto_increment primary key not null,\n" +
      "    CREATIONTIME TIMESTAMP,\n" +
      "    UPDATETIME TIMESTAMP,\n" +
      "    VERSION BIGINT NOT NULL,\n" +
      "    NAME VARCHAR(4096) NOT NULL,\n" +
      "    CATEGORY VARCHAR(255) NOT NULL,\n" +
      "    REGEX VARCHAR(1000) NOT NULL,\n" +
      "    SIMPLECONTAINS BOOLEAN NOT NULL\n" +
      ")";
    String bookingCsvTemplate = "CREATE TABLE PUBLIC.BOOKINGCSVTEMPLATE(\n" +
      "id bigint(19) auto_increment primary key not null,\n" +
      "    CREATIONTIME TIMESTAMP,\n" +
      "    UPDATETIME TIMESTAMP,\n" +
      "    VERSION BIGINT NOT NULL,\n" +
      "    NAME VARCHAR(4096) NOT NULL,\n" +
      "    AMOUNTCOLUMNS VARCHAR(255),\n" +
      "    DATECOLUMN INTEGER NOT NULL,\n" +
      "    DATEPATTERN VARCHAR(255),\n" +
      "    DESCRIPTIONCOLUMN INTEGER NOT NULL,\n" +
      "    SEPARATOR VARCHAR(255),\n" +
      "    TIMECOLUMN INTEGER,\n" +
      "    TIMEPATTERN VARCHAR(255),\n" +
      "    USECOMMA BOOLEAN NOT NULL,\n" +
      "    ACCOUNT_ID BIGINT NOT NULL\n" +
      ")";
    String account = "CREATE TABLE PUBLIC.ACCOUNT(\n" +
      "id bigint(19) auto_increment primary key not null,\n" +
      "    CREATIONTIME TIMESTAMP,\n" +
      "    UPDATETIME TIMESTAMP,\n" +
      "    VERSION BIGINT NOT NULL,\n" +
      "    NAME VARCHAR(4096) NOT NULL,\n" +
      "    OWNER VARCHAR(255)\n" +
      ")";
    String booking = "CREATE TABLE PUBLIC.BOOKING(\n" +
      "id bigint(19) auto_increment primary key not null,\n" +
      "    CREATIONTIME TIMESTAMP,\n" +
      "    UPDATETIME TIMESTAMP,\n" +
      "    VERSION BIGINT NOT NULL,\n" +
      "    AMOUNT DOUBLE NOT NULL,\n" +
      "    BOOKINGTIME TIMESTAMP NOT NULL,\n" +
      "    CATEGORY VARCHAR(255),\n" +
      "    DESCRIPTION VARCHAR(1000),\n" +
      "    INTERNALIDENTIFIER VARCHAR(255),\n" +
      "    ACCOUNT_ID BIGINT NOT NULL\n" +
      ")";
    String scheduledBooking = "CREATE CACHED TABLE PUBLIC.SCHEDULEDBOOKING(\n" +
      "id bigint(19) auto_increment primary key not null,\n" +
      "    CREATIONTIME TIMESTAMP,\n" +
      "    UPDATETIME TIMESTAMP,\n" +
      "    VERSION BIGINT NOT NULL,\n" +
      "    NAME VARCHAR(4096) NOT NULL,\n" +
      "    AMOUNT DOUBLE NOT NULL,\n" +
      "    SCHEDULE_ID BIGINT\n" +
      ")";

    String scheduleDuration = "alter table schedule add duration bigint(19)\n";

    executeStatement(account);
    executeStatement(booking);
    executeStatement(bookingPatternTable);
    executeStatement(bookingCsvTemplate);
    executeStatement(scheduledBooking);
    executeStatement(scheduleDuration);
  }
}
