package de.ks.workflow.validation.event;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.validation.ConstraintViolation;
import java.lang.reflect.Field;
import java.util.Set;

/**
 *
 */
public class ValidationResultEvent {
  private boolean successful;
  private Field field;
  private final Object value;
  private final Set<ConstraintViolation<Object>> violations;

  public ValidationResultEvent(boolean successful, Field field, Object value, Set<ConstraintViolation<Object>> violations) {
    this.successful = successful;
    this.field = field;
    this.value = value;
    this.violations = violations;
  }

  public boolean isSuccessful() {
    return successful;
  }

  public Field getValidatedField() {
    return field;
  }

  public Set<ConstraintViolation<Object>> getViolations() {
    return violations;
  }

  public Object getValue() {
    return value;
  }
}
