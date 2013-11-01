package de.ks.eventsystem.bus;/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

/**
 * Specifies the target for an event.
 */
public enum EventTarget {
  /**
   * Forwards the event to the CDI container.
   */
  CDI,
  /**
   * Forwards the events to the registered handlers.
   */
  Default;
}
