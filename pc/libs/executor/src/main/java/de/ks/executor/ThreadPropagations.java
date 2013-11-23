package de.ks.executor;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ThreadPropagations {
  private Map<Thread,Object> propagations = new HashMap<>();

  public void register(Object value) {
    propagations.put(Thread.currentThread(),value);
  }

  public Map<Thread,?> getPropagations() {
    return propagations;
  }
}
