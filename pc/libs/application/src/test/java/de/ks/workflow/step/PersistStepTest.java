package de.ks.workflow.step;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.JFXCDIRunner;
import de.ks.persistence.PersistentWork;
import de.ks.workflow.SimpleWorkflowModel;
import de.ks.workflow.TestWorkflow;
import de.ks.workflow.WorkflowNavigator;
import de.ks.workflow.cdi.WorkflowContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaQuery;

import static org.junit.Assert.assertEquals;

/**
 *
 */
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
        CriteriaQuery<SimpleWorkflowModel> query = builder.createQuery(SimpleWorkflowModel.class);
        query.select(query.from(SimpleWorkflowModel.class));
        SimpleWorkflowModel model = em.createQuery(query).getSingleResult();
        assertEquals("testName", model.getName());
        assertEquals("Hello Sauerland!", model.getDescription());
      }
    };
  }
}
