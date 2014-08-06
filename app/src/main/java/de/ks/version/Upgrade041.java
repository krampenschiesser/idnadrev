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

public class Upgrade041 extends PersistentVersionUpgrade {
  private static final Logger log = LoggerFactory.getLogger(Upgrade041.class);

  public Upgrade041() {
    super("");
  }

  @Override
  public int getVersion() {
    return 41;
  }

  @Override
  public void performUpgrade() {
    log.info("Create schedule table and reference to task for version upgrade from 0.4.0 to 0.4.1");

    String addScheduleColumn = "alter table task add state varchar(255)\n";
    String fillColumn = "update task set state = 'NONE'\n";

    executeStatement(addScheduleColumn);
    executeStatement(fillColumn);
  }
}
