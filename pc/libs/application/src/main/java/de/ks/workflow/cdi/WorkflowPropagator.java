/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ks.workflow.cdi;


import de.ks.executor.ThreadCallBoundValue;
import de.ks.workflow.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import java.util.LinkedList;

/**
 *
 */
@Vetoed
public class WorkflowPropagator implements ThreadCallBoundValue {
  private static final Logger log = LoggerFactory.getLogger(WorkflowPropagator.class);
  protected final WorkflowContext context;
  private Class<? extends Workflow> propagatedWorkflowId;
  private String propagatedWorkflowSequence;

  @Inject
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
      log.debug("Nothing to propagate {}", propagatedWorkflowSequence);
    }
  }

  @Override
  public void doAfterCallInTargetThread() {
    if (propagatedWorkflowId != null) {
      log.debug("Stopping workflow {}->{}", propagatedWorkflowSequence, propagatedWorkflowId.getSimpleName());
      context.stopWorkflow(propagatedWorkflowId);
    } else {
      log.debug("Nothing to stopActivity {}", propagatedWorkflowSequence);
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
