package de.ks.workflow.step;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.persistence.PersistentWork;
import de.ks.workflow.WorkflowState;
import javafx.concurrent.Task;

import javax.inject.Inject;

/**
 *
 */
public class PersistStep extends AutomaticStep {
  @Inject
  WorkflowState workflowState;

  @Override
  public Task<String> getTask() {
    return new Task<String>() {
      @Override
      protected String call() throws Exception {
        Object model = workflowState.getModel();
        new PersistentWork() {
          @Override
          protected void execute() {
            em.persist(model);
          }
        };
        return DefaultOutput.NEXT.name();
      }
    };
  }
}
