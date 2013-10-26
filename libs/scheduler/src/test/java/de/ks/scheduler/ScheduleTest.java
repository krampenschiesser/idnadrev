package de.ks.scheduler;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

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
}
