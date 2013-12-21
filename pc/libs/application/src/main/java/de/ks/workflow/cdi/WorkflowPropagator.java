package de.ks.workflow.cdi;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.executor.ThreadCallBoundValue;
import de.ks.workflow.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

/**
 *
 */
public class WorkflowPropagator implements ThreadCallBoundValue {
  private static final Logger log = LoggerFactory.getLogger(WorkflowPropagator.class);
  protected final WorkflowContext context;
  private Class<? extends Workflow> propagatedWorkflowId;
  private String propagatedWorkflowSequence;

  public WorkflowPropagator(WorkflowContext context) {
    this.context = context;
  }

  @Override
  public void initializeInCallerThread() {
    LinkedList<Class<? extends Workflow>> workflowIds = context.workflowStack.get();
    if (!workflowIds.isEmpty()) {
      propagatedWorkflowId = workflowIds.getLast();
      propagatedWorkflowSequence = context.getHolder().getId();
      context.registerPlannedPropagation(propagatedWorkflowId);
    }
  }

  @Override
  public void doBeforeCallInTargetThread() {
    if (propagatedWorkflowId != null) {
      log.debug("Propagating workflow {}->{}", propagatedWorkflowSequence, propagatedWorkflowId.getSimpleName());
      context.propagateWorkflow(propagatedWorkflowId);
    } else {
      log.debug("Nothing to propagate {}->{}", propagatedWorkflowSequence, propagatedWorkflowId != null ? propagatedWorkflowId.getSimpleName() : null);
    }
  }

  @Override
  public void doAfterCallInTargetThread() {
    if (propagatedWorkflowId != null) {
      log.debug("Stopping workflow {}->{}", propagatedWorkflowSequence, propagatedWorkflowId.getSimpleName());
      context.stopWorkflow(propagatedWorkflowId);
    } else {
      log.debug("Nothing to stop {}->{}", propagatedWorkflowSequence, propagatedWorkflowId != null ? propagatedWorkflowId.getSimpleName() : null);
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
