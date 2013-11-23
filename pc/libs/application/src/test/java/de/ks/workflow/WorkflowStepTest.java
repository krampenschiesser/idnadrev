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
