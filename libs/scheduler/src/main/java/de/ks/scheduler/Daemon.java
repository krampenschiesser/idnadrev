package de.ks.scheduler;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by lastTime, license may come later.
 */

import javax.annotation.PreDestroy;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.HashSet;
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
  private final Set<Crontab> cronTabs = new HashSet<>();
  private final ScheduledFuture<?> schedule;
  private Temporal lastTime;

  private Temporal fixedTime;

  public Daemon() {
    executorService = Executors.newScheduledThreadPool(1);
    schedule = executorService.schedule(() -> run(), 30, TimeUnit.SECONDS);
    lastTime = LocalTime.now();
  }

  private void run() {
    for (Crontab cronTab : cronTabs) {
//      if(cronTab.getMinutes())
    }

//    LocalTime now = LocalTime.now();
//    Duration between = Duration.between(lastTime, LocalTime.now());
//    between.
  }

  /**
   * Registers a crontab directly. Now further start or schedule needed.
   *
   * @param crontab
   * @return
   */
  public Daemon addCrontab(Crontab crontab) {
    this.cronTabs.add(crontab);
    return this;
  }


  //Test purpose
  void mockTime(Temporal temporal) {
    this.fixedTime = temporal;
  }

  @PreDestroy
  public void shutdown() {
    schedule.cancel(false);
    cronTabs.clear();
    executorService.shutdownNow();
  }
}
