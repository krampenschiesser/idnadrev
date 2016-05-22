/*
 * Copyright [2016] [Christian Loehnert]
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

package de.ks.idnadrev.task.cron;

import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.Assert.assertEquals;

public class ProposedWeekTest {
  @Test
  public void testMonday() throws Exception {
    ProposedWeek proposedWeek = new ProposedWeek(2016, 42);
    LocalDate monday = proposedWeek.getMonday();
    assertEquals(Month.OCTOBER, monday.getMonth());
    assertEquals(10, monday.getDayOfMonth());
  }

  @Test
  public void testMondayFirstWeek() throws Exception {
    ProposedWeek proposedWeek = new ProposedWeek(2016, 1);
    LocalDate monday = proposedWeek.getMonday();
    assertEquals(Month.DECEMBER, monday.getMonth());
    assertEquals(28, monday.getDayOfMonth());
  }

  @Test
  public void testMondayLastWeek() throws Exception {
    ProposedWeek proposedWeek = new ProposedWeek(2016, 53);
    LocalDate monday = proposedWeek.getMonday();
    assertEquals(Month.DECEMBER, monday.getMonth());
    assertEquals(26, monday.getDayOfMonth());
  }
}