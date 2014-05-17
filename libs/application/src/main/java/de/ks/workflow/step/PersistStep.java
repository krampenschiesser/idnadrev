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

package de.ks.workflow.step;


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
        log.debug("Persisting worfklow model {}", model);
        PersistentWork.run(em -> em.persist(model));
        return DefaultOutput.NEXT.name();
      }
    };
  }
}
