package de.ks.executor;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class SimpleThreadFactory implements ThreadFactory {
  protected AtomicInteger count = new AtomicInteger(0);

  @Override
  public Thread newThread(Runnable r) {
    Thread thread = new Thread(r);
    thread.setName("KSPool-" + count.incrementAndGet());
    return thread;
  }
}
