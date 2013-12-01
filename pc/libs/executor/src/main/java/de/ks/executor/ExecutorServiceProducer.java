package de.ks.executor;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.enterprise.inject.Produces;

/**
 *
 */
public class ExecutorServiceProducer {

  @Produces
  public ExecutorService get() {
    return ExecutorService.instance;
  }
}
