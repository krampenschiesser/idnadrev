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
package de.ks.version;

import de.ks.persistence.PersistentVersionUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Upgrade040 extends PersistentVersionUpgrade {
  private static final Logger log = LoggerFactory.getLogger(Upgrade040.class);

  public Upgrade040() {
    super("");
  }

  @Override
  public int getVersion() {
    return 40;
  }

  @Override
  public void performUpgrade() {
    log.info("Create schedule table and reference to task for version upgrade from 0.3.0 to 0.4.0");
    String createScheduleTable = "\n" +
            "CREATE TABLE schedule\n" +
            "(\n" +
            "id bigint(19) auto_increment primary key not null,\n" +
            "version bigint(19) not null,\n" +
            "proposedweek int(10) not null,\n" +
            "proposedweekday varchar(255),\n" +
            "repetition varchar(255),\n" +
            "scheduleddate varchar(250),\n" +
            "scheduledtime varchar(250),\n" +
            ")\n";
    String dropScheduleColumn = "ALTER TABLE TASK\n" + "drop column SCHEDULE_ID\n";
    String addScheduleColumn = "alter table task add SCHEDULE_ID bigint(19)\n";
    String createScheduleIDX = "CREATE INDEX IDX_TASK_SCHEDULE_ID ON TASK(SCHEDULE_ID)\n";
    String addForeignKey = "ALTER TABLE TASK\n" +
            "ADD FOREIGN KEY (SCHEDULE_ID) \n" +
            "REFERENCES SCHEDULE(ID)\n";

    executeStatement(createScheduleTable);
    executeStatement(dropScheduleColumn);
    executeStatement(addScheduleColumn);
    executeStatement(createScheduleIDX);
    executeStatement(addForeignKey);
  }
}
