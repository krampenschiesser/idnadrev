package de.ks.eventsystem.bus;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

/**
 *
 */
class EventExecution {
  protected Object event;
  protected EventHandler handler;

  public EventExecution(Object event, EventHandler handler) {
    this.event = event;
    this.handler = handler;
  }

}
