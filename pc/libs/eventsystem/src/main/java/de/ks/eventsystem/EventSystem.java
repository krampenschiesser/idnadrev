package de.ks.eventsystem;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.enterprise.inject.Produces;

public class EventSystem {
  private static final Logger log = LogManager.getLogger(EventSystem.class);

  public static final EventBus bus;

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
