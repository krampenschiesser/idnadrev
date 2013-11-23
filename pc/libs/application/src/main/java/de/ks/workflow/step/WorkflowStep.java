package de.ks.workflow.step;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.step.DefaultOutput;

/**
 *
 */
public abstract class WorkflowStep {
  public String getTitle() {
    return this.getClass().getName();
  }

  public String getOutputValue() {
    return DefaultOutput.NEXT.name();
  }
}
