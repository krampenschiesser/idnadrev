package de.ks.workflow.cdi;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.executor.ThreadCallBoundValue;
import de.ks.workflow.Workflow;

import java.util.LinkedList;

/**
 *
 */
public class WorkflowPropagator implements ThreadCallBoundValue {
  protected final WorkflowContext context;
  private Class<? extends Workflow> propagatedWorkflowId;
  private Class<? extends Workflow> previousWorkflowId = null;

  public WorkflowPropagator(WorkflowContext context) {
    this.context = context;
  }

  @Override
  public void initializeInCallerThread() {
    LinkedList<Class<? extends Workflow>> workflowIds = context.workflowStack.get();
    if (!workflowIds.isEmpty()) {
      propagatedWorkflowId = workflowIds.getLast();
    }
  }

  @Override
  public void doBeforeCallInTargetThread() {
    if (propagatedWorkflowId != null) {
      if (!context.workflowStack.get().isEmpty()) {
        previousWorkflowId = context.workflowStack.get().getLast();
      }
      context.propagateWorkflow(propagatedWorkflowId);
    }
  }

  @Override
  public void doAfterCallInTargetThread() {
    if (propagatedWorkflowId != null) {
      context.stopWorkflow(propagatedWorkflowId);
      if (previousWorkflowId != null) {
        context.propagateWorkflow(previousWorkflowId);
      }
    }
  }

  public WorkflowPropagator clone() {
    try {
      WorkflowPropagator clone = (WorkflowPropagator) super.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new InternalError("Could not clone " + getClass().getName());
    }
  }
}
