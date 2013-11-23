package de.ks.workflow.validation.event;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import java.lang.reflect.Field;

/**
 *
 */
public class ValidationRequiredEvent {
  private final Object value;
  private final Field field;

  public ValidationRequiredEvent(Field field, Object value) {
    this.field = field;
    this.value = value;
  }

  public Object getValue() {
    return value;
  }

  public Field getField() {
    return field;
  }

  @Override
  public String toString() {
    return "ValidationRequiredEvent{" +
            "value=" + value +
            ", field=" + field +
            '}';
  }
}
