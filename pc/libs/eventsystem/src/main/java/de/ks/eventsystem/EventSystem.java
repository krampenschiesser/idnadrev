package de.ks.eventsystem;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import de.ks.eventsystem.bus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Produces;

public class EventSystem {
  private static final Logger log = LoggerFactory.getLogger(EventSystem.class);

  public static final EventBus bus;

  public static void setWaitForEvents(boolean wait) {
    EventBus.alwaysWait = wait;
  }

  static {
    bus = new EventBus();
    bus.register(new EventSystem());
  }

  @Subscribe
  public void onDeadEvent(DeadEvent dead) {
    log.warn("No handler for event \"{}\" found. Contents: {}", dead.getEvent().getClass().getSimpleName(), dead.getEvent());
  }


  @Produces
  public EventBus getEventBus() {
    return bus;
  }
}
