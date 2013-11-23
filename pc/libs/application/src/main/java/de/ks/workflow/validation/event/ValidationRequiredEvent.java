package de.ks.workflow.validation.event;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.reflection.PropertyPath;

/**
 *
 */
public class ValidationRequiredEvent {
  private final PropertyPath<?> path;
  private final Object value;

  public ValidationRequiredEvent(PropertyPath<?> path, Object value) {
    this.path = path;
    this.value = value;
  }

  public PropertyPath<?> getPath() {
    return path;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "ValidationRequiredEvent{" +
            "path=" + path +
            ", value=" + value +
            '}';
  }
}
