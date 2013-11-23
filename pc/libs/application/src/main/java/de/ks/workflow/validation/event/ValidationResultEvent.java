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
public class ValidationResultEvent {
  private boolean successful;
  private Field field;

  public ValidationResultEvent(boolean successful) {
    this.successful = successful;
  }

  public boolean isSuccessful() {
    return successful;
  }

  public Field getValidatedField() {
    return field;
  }
}
