package de.ks.scheduler.event;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */


/**
 *
 */
public class ScheduleTriggeredEvent {
  protected final Object userData;

  public ScheduleTriggeredEvent(Object userData) {
    this.userData = userData;
  }

  public Object getUserData() {
    return userData;
  }
}