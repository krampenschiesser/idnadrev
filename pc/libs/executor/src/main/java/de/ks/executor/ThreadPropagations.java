package de.ks.executor;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class ThreadPropagations {
  private HashSet<ThreadCallBoundValue> propagations = new HashSet<>();

  public void register(ThreadCallBoundValue value) {
    propagations.add(value);
  }

  public Set<ThreadCallBoundValue> getPropagations() {
    HashSet<ThreadCallBoundValue> retval = new HashSet<>();
    for (ThreadCallBoundValue propagation : propagations) {
      retval.add(propagation.clone());
    }
    return retval;
  }
}
