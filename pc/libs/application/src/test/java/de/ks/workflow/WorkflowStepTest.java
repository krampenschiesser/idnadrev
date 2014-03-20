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

package de.ks.workflow;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.JFXCDIRunner;
import de.ks.workflow.cdi.WorkflowContext;
import de.ks.workflow.step.DefaultOutput;
import de.ks.workflow.step.WorkflowStepConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.CDI;

import static org.junit.Assert.*;

/**
 *
 */
@RunWith(JFXCDIRunner.class)
public class WorkflowStepTest {

  private TestWorkflow workflow;

  @After
  public void tearDown() throws Exception {
    WorkflowContext.stopAll();
  }

  @Before
  public void setUp() throws Exception {
    WorkflowContext.start(TestWorkflow.class);
    workflow = CDI.current().select(TestWorkflow.class).get();
  }

  @Test
  public void testModel() throws Exception {
    assertNotNull(workflow.getModel());
    assertEquals(SimpleWorkflowModel.class, workflow.getModel().getClass());
  }

  @Test
  public void testSteps() throws Exception {
    WorkflowConfig cfg = CDI.current().select(WorkflowConfig.class).get();
    WorkflowStepConfig root = cfg.getRoot();

    assertNotNull(root);
    assertTrue(root.hasOutput(DefaultOutput.NEXT.name()));
  }
}
