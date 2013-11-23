package de.ks.workflow.cdi;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.enterprise.util.AnnotationLiteral;

public class WorkflowSpecificLiteral extends AnnotationLiteral<WorkflowSpecific> implements WorkflowSpecific {
  private final String value;

  public WorkflowSpecificLiteral(String value) {
    this.value = value;
  }

  @Override
  public String value() {
    return value;
  }
}
