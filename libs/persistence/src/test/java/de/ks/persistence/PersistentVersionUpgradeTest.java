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

import org.junit.Before;
import org.junit.Test;

public class PersistentVersionUpgradeTest {
  private PersistentVersionUpgrade persistentVersionUpgrade;

  @Before
  public void setUp() throws Exception {
    persistentVersionUpgrade = new PersistentVersionUpgrade("") {

      @Override
      public int getVersion() {
        return 0;
      }

      @Override
      public void performUpgrade() {

      }
    };
  }

  @Test
  public void testExecuteStatement() throws Exception {
    persistentVersionUpgrade.executeStatement("select count(1) from DummyEntity");
  }
}