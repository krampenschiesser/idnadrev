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

public class Upgrade042 extends PersistentVersionUpgrade {
  private static final Logger log = LoggerFactory.getLogger(Upgrade042.class);

  public Upgrade042() {
    super("");
  }

  @Override
  public int getVersion() {
    return 42;
  }

  @Override
  public void performUpgrade() {
    log.info("Create proposed year column in schedule for version upgrade from 0.4.1 to 0.4.2");
    String addScheduleColumn = "alter table schedule add proposedyear integer(10) default 0 not null\n";
    executeStatement(addScheduleColumn);
  }
}
