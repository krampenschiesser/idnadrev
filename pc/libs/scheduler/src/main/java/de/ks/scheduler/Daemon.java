/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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
 * All rights reserved by lastTime, license may come later.
 */

import de.ks.eventsystem.EventSystem;
import de.ks.executor.ExecutorService;
import de.ks.scheduler.event.ScheduleTriggeredEvent;

import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class Daemon {
  private final Map<Schedule, Object> schedules = new HashMap<>();
  private final ScheduledFuture<?> future;

  private LocalDateTime fixedTime;

  public Daemon() {
    future = ExecutorService.instance.schedule(() -> trigger(), 15, TimeUnit.SECONDS);
  }

  public void trigger() {
    Set<Map.Entry<Schedule, Object>> entries = schedules.entrySet();
    for (Map.Entry<Schedule, Object> entry : entries) {
      Schedule schedule = entry.getKey();
      Object userData = entry.getValue();
      //Test mock case
      if (fixedTime != null) {
        if (schedule.isScheduledToday(fixedTime.toLocalDate())) {
          if (schedule.getScheduledTime() == null || schedule.isScheduledNow(fixedTime.toLocalTime())) {
            triggerSchedule(userData);
          }
        }
        //Test mock case END
      } else {
        if (schedule.isScheduledToday()) {
          if (schedule.getScheduledTime() == null || schedule.isScheduledNow()) {
            triggerSchedule(userData);
          }
        }
      }


    }
  }


  /**
   * Registers a schedule directly. Now further start or schedule needed.
   *
   * @param schedule
   * @return
   */
  public Daemon addSchedule(Schedule schedule, Object userDate) {
    schedules.put(schedule, userDate);
    return this;
  }

  private void triggerSchedule(Object userData) {
    EventSystem.bus.post(new ScheduleTriggeredEvent(userData));
  }


  //Test purpose
  void mockTime(LocalDateTime time) {
    future.cancel(true);
    this.fixedTime = time;
  }

  @PreDestroy
  public void shutdown() {
    future.cancel(false);
    schedules.clear();
  }

}
