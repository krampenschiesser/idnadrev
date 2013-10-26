package de.ks.scheduler;/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.eventbus.Subscribe;
import de.ks.eventsystem.EventSystem;
import de.ks.scheduler.event.ScheduleTriggeredEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class DaemonTest {
  private Daemon daemon;
  private Object userData;

  @Before
  public void setUp() throws Exception {
    daemon = new Daemon();
    EventSystem.bus.register(this);
  }

  @After
  public void tearDown() throws Exception {
    EventSystem.bus.unregister(this);
  }

  @Test
  public void testScheduling() throws Exception {
    Schedule schedule = new Schedule(LocalDate.now());
    daemon.addSchedule(schedule, "buh");
    daemon.mockTime(LocalDateTime.now());
    daemon.trigger();
    assertEquals("buh", userData);
  }

  @Subscribe
  public void onTaskScheduled(ScheduleTriggeredEvent event) {
    userData = event.getUserData();
  }
}
