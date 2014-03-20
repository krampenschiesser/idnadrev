/*
 * Copyright [${YEAR}] [Christian Loehnert]
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

package de.ks.scheduler;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class ScheduleTest {

  private Schedule schedule;

  @Before
  public void setUp() throws Exception {
    schedule = new Schedule(LocalDate.of(2013, 10, 3));
    schedule.setScheduledTime(LocalTime.of(12, 30, 32));
  }

  @Test
  public void testRepetitions() throws Exception {
    schedule.setRepetition(Schedule.RepetitionType.DAILY);
    assertTrue(schedule.isScheduledToday(LocalDate.now()));

    schedule.setRepetition(Schedule.RepetitionType.WEEKLY);
    assertTrue(schedule.isScheduledToday(LocalDate.of(2013, 10, 10)));
    assertTrue(schedule.isScheduledToday(LocalDate.of(2013, 10, 17)));
    assertFalse(schedule.isScheduledToday(LocalDate.of(2013, 9, 26)));
    assertFalse(schedule.isScheduledToday(LocalDate.of(2013, 10, 9)));
    assertFalse(schedule.isScheduledToday(LocalDate.of(2013, 10, 11)));

    schedule.setRepetition(Schedule.RepetitionType.MONTHLY);
    assertTrue(schedule.isScheduledToday(LocalDate.of(2013, 11, 3)));
    assertFalse(schedule.isScheduledToday(LocalDate.of(2013, 11, 2)));
    assertFalse(schedule.isScheduledToday(LocalDate.of(2013, 9, 26)));

    schedule.setRepetition(Schedule.RepetitionType.YEARLY);
    assertTrue(schedule.isScheduledToday(LocalDate.of(2014, 10, 3)));
    assertFalse(schedule.isScheduledToday(LocalDate.of(2014, 11, 3)));
  }

  @Test
  public void testScheduledNow() throws Exception {
    assertTrue(schedule.isScheduledNow(LocalTime.of(12, 30, 45)));
    assertFalse(schedule.isScheduledNow(LocalTime.of(12, 31, 32)));
    assertFalse(schedule.isScheduledNow(LocalTime.of(12, 29, 32)));

  }
}
