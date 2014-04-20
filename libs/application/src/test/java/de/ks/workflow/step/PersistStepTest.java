/*
 * Copyright [${YEAR}] [Christian Loehnert]
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


import de.ks.JFXCDIRunner;
import de.ks.persistence.PersistentWork;
import de.ks.workflow.SimpleWorkflowModel;
import de.ks.workflow.TestWorkflow;
import de.ks.workflow.cdi.WorkflowContext;
import de.ks.workflow.navigation.WorkflowNavigator;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaQuery;

import static org.junit.Assert.assertEquals;

/**
 *
 */
@Ignore
@RunWith(JFXCDIRunner.class)
public class PersistStepTest {
  @Inject
  TestWorkflow workflow;
  @Inject
  WorkflowNavigator navigator;


  @After
  public void tearDown() throws Exception {
    WorkflowContext.stopAll();
  }

  @Before
  public void setUp() throws Exception {
    WorkflowContext.start(TestWorkflow.class);
    workflow = CDI.current().select(TestWorkflow.class).get();
    navigator.start();
  }

  @Test
  public void testPersistStep() throws Exception {
    SimpleWorkflowModel model = workflow.getModel();
    model.setName("testName");
    model.setDescription("Hello Sauerland!");
    navigator.next();

    new PersistentWork() {
      @Override
      protected void execute() {
        CriteriaQuery<SimpleWorkflowModel> query = em.getCriteriaBuilder().createQuery(SimpleWorkflowModel.class);
        query.select(query.from(SimpleWorkflowModel.class));
        SimpleWorkflowModel model = em.createQuery(query).getSingleResult();
        assertEquals("testName", model.getName());
        assertEquals("Hello Sauerland!", model.getDescription());
      }
    };
  }
}
