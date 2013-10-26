package de.ks.eventsystem;

import com.google.common.eventbus.EventBus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */
public class EventSystem {
  public static final EventBus bus;

  static {
    ExecutorService executor = Executors.newWorkStealingPool();
//    bus = new AsyncEventBus(executor);
    bus = new EventBus();
  }
}
