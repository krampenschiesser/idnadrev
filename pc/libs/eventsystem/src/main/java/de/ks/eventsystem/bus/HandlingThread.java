package de.ks.eventsystem.bus;/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

/**
 * Specifies the event handling thread a handler
 */
public enum HandlingThread {
  /**
   * Execute in same thread, default case
   */
  Sync,
  /**
   * Execute asynchronously in the default {@link java.util.concurrent.ForkJoinPool#commonPool()}
   */
  Async,
  /**
   * Execute in JavaFX application thread
   */
  JavaFX;
}
