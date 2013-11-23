package de.ks.workflow.cdi;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.executor.ThreadCallBoundValue;

import java.util.LinkedList;

/**
 *
 */
public class WorkflowPropagator implements ThreadCallBoundValue {
  protected final WorkflowContext context;
  private String propagatedWorkflowId;
  private String previousWorkflowId = null;

  public WorkflowPropagator(WorkflowContext context) {
    this.context = context;
  }

  @Override
  public void initializeInCallerThread() {
    LinkedList<String> workflowIds = context.workflowStack.get();
    propagatedWorkflowId = workflowIds.getLast();
  }

  @Override
  public void doBeforeCallInTargetThread() {
    if (!context.workflowStack.get().isEmpty()) {
      previousWorkflowId = context.workflowStack.get().getLast();
    }
    context.propagateWorkflow(propagatedWorkflowId);
  }

  @Override
  public void doAfterCallInTargetThread() {
    context.stopWorkflow(propagatedWorkflowId);
    if (previousWorkflowId != null) {
      context.propagateWorkflow(previousWorkflowId);
    }
  }
}
