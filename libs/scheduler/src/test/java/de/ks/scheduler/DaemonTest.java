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

package de.ks.scheduler;

import com.google.common.eventbus.Subscribe;
import de.ks.LauncherRunner;
import de.ks.eventsystem.bus.EventBus;
import de.ks.scheduler.event.ScheduleTriggeredEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.CDI;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class DaemonTest {
  private Daemon daemon;
  private Object userData;
  private EventBus bus;

  @Before
  public void setUp() throws Exception {
    daemon = new Daemon();
    bus = CDI.current().select(EventBus.class).get();
    bus.register(this);
  }

  @After
  public void tearDown() throws Exception {
    bus.unregister(this);
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
