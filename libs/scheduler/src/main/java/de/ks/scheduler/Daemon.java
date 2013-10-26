package de.ks.scheduler;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by lastTime, license may come later.
 */

import de.ks.eventsystem.EventSystem;
import de.ks.scheduler.event.ScheduleTriggeredEvent;

import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class Daemon {
  private final ScheduledExecutorService executorService;
  private final Map<Schedule, Object> schedules = new HashMap<>();
  private final ScheduledFuture<?> future;

  private LocalDateTime fixedTime;

  public Daemon() {
    executorService = Executors.newScheduledThreadPool(1);
    future = executorService.schedule(() -> trigger(), 15, TimeUnit.SECONDS);
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
   * Registers a crontab directly. Now further start or schedule needed.
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
    executorService.shutdownNow();
  }

}
