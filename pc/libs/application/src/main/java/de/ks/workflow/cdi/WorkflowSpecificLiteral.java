package de.ks.workflow.cdi;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.Workflow;

import javax.enterprise.util.AnnotationLiteral;

public class WorkflowSpecificLiteral extends AnnotationLiteral<WorkflowSpecific> implements WorkflowSpecific {
  private final Class<? extends Workflow> value;

  public WorkflowSpecificLiteral(Class<? extends Workflow> value) {
    this.value = value;
  }

  @Override
  public Class<? extends Workflow> value() {
    return value;
  }
}
