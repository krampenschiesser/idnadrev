package de.ks.executor;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import java.util.concurrent.ThreadFactory;

/**
 *
 */
public class SimpleThreadFactory implements ThreadFactory {

  @Override
  public Thread newThread(Runnable r) {
    Thread thread = new Thread(r);
    return thread;
  }
}
