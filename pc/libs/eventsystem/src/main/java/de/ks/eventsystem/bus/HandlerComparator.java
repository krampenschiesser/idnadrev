package de.ks.eventsystem.bus;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import java.util.Comparator;

/**
 *
 */
class HandlerComparator implements Comparator<EventHandler> {
  @Override
  public int compare(EventHandler o1, EventHandler o2) {
    return o1.priority.compareTo(o2.priority);
  }
}
