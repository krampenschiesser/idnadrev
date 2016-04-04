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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.*;

public class CronTabTest {
  @Test
  public void testEveryDayAtNoon() throws Exception {
    CronTab cronTab = new CronTab();
    cronTab.parse("0 12 * * *");

    LocalDateTime now = LocalDateTime.of(2016, 4, 2, 16, 13);
    Optional<LocalDateTime> nextDateTime = cronTab.getNextDateTime(now);
    assertTrue(nextDateTime.isPresent());
    assertEquals(LocalDateTime.of(2016, 4, 3, 12, 00), nextDateTime.get());
  }

  @Test
  public void testProposedWeek() throws Exception {
    CronTab cronTab = new CronTab();
    cronTab.parse("* * * * 2016 16");

    Optional<ProposedWeek> proposedWeek = cronTab.getProposedWeek();
    assertTrue(proposedWeek.isPresent());
    assertEquals(16, proposedWeek.get().getWeek());
    assertEquals(2016, proposedWeek.get().getYear());
  }

  @Test
  public void testProposedWeekDay() throws Exception {
    CronTab cronTab = new CronTab();
    cronTab.parse("* * * * 2016 16 1");

    assertFalse(cronTab.getProposedWeek().isPresent());
    Optional<ProposedWeekDay> proposedWeekDay = cronTab.getProposedWeekDay();
    assertTrue(proposedWeekDay.isPresent());
    assertEquals(1, proposedWeekDay.get().getDayOfWeek());
    assertEquals(16, proposedWeekDay.get().getWeek());
    assertEquals(2016, proposedWeekDay.get().getYear());
  }

  @Test
  public void testProposedDay() throws Exception {
    CronTab cronTab = new CronTab();
    cronTab.parse("? ? 24 11 2016");

    Optional<LocalDate> proposedDate = cronTab.getProposedDate();
    assertTrue(proposedDate.isPresent());
    assertEquals(LocalDate.of(2016, 11, 24), proposedDate.get());
  }

  @Test
  public void testNextProposedDay() throws Exception {
    CronTab cronTab = new CronTab();
    cronTab.parse("? ? 5 * 2016");
    LocalDate now = LocalDate.of(2016, 11, 24);

    assertFalse(cronTab.getProposedDate().isPresent());

    Optional<LocalDate> nextProposedDate = cronTab.getNextProposedDate(now);
    assertTrue(nextProposedDate.isPresent());
    assertEquals(LocalDate.of(2016, 12, 5), nextProposedDate.get());
  }
}
