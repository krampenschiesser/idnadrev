package de.ks.scheduler;/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalTime;

public class DaemonTest {
  private Daemon daemon;

  @Before
  public void setUp() throws Exception {
    daemon = new Daemon();
  }

  @Test
  public void testScheduling() throws Exception {
    Crontab crontab = new Crontab("3 12 * * *");
    daemon.addCrontab(crontab);
    Instant now = Instant.now();
    LocalTime offsetTime = LocalTime.now().withHour(12).withMinute(3).withSecond(0);
    daemon.mockTime(offsetTime.adjustInto(LocalTime.now()));
  }
}
