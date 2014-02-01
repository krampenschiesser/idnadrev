package de.ks.workflow.step;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.persistence.PersistentWork;
import de.ks.workflow.WorkflowState;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 *
 */
public class PersistStep extends AutomaticStep {
  private static final Logger log = LoggerFactory.getLogger(PersistStep.class);
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
            log.debug("Persisting worfklow model {}", model);
            em.persist(model);
          }
        };
        return DefaultOutput.NEXT.name();
      }
    };
  }
}
