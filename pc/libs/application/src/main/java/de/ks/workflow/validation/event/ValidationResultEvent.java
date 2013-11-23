package de.ks.workflow.validation.event;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

/**
 *
 */
public class ValidationResultEvent {
  private boolean successful;

  public ValidationResultEvent(boolean successful) {
    this.successful = successful;
  }

  public boolean isSuccessful() {
    return successful;
  }
}
