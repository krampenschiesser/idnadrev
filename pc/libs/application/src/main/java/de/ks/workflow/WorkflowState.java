package de.ks.workflow;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.cdi.WorkflowContext;
import de.ks.workflow.cdi.WorkflowScoped;

import javax.inject.Inject;

/**
 *
 */
@WorkflowScoped
public class WorkflowState {
  @Inject
  protected WorkflowContext context;

  public Object getModel() {
    return context.getWorkflow().getModel();
  }

  public Class<?> getModelClass() {
    return context.getWorkflow().getModelClass();
  }

  public Class<? extends Workflow> getWorkflowClass() {
    return context.getWorkflow().getClass();
  }
}
