package de.ks.workflow;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.eventbus.Subscribe;
import de.ks.JFXCDIRunner;
import de.ks.eventsystem.bus.EventBus;
import de.ks.reflection.PropertyPath;
import de.ks.workflow.cdi.WorkflowContext;
import de.ks.workflow.validation.event.ValidationRequiredEvent;
import de.ks.workflow.validation.event.ValidationResultEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.*;

/**
 *
 *
 */
@RunWith(JFXCDIRunner.class)
@Ignore
public class WorkflowValidationTest {
  private PropertyPath<SimpleWorkflowModel> path;
  private ValidationResultEvent result;

  @Inject
  SimpleWorkflowModel model;
  @Inject
  EventBus eventBus;
  //  @Inject
  Workflow state;

  @Before
  public void setUp() throws Exception {
    path = PropertyPath.of(SimpleWorkflowModel.class);
    path.build().setName(null);
    eventBus.register(this);
  }

  @After
  public void tearDown() throws Exception {
    WorkflowContext.stopAll();
  }

  @Test
  public void testModelInitialization() throws Exception {
    WorkflowContext.start(TestWorkflow.class);
    Object model = state.getModel();
    assertNotNull(model);
  }

  @Test
  public void testModelValidation() throws Exception {
    WorkflowContext.start(TestWorkflow.class);

    eventBus.postAndWait(new ValidationRequiredEvent(null, null));
    Thread.sleep(500);
    assertNotNull(result);
    assertFalse(result.isSuccessful());

    eventBus.postAndWait(new ValidationRequiredEvent(null, "hello"));
    assertNotNull(result);
    assertTrue(result.isSuccessful());
  }

  @Subscribe
  public void onValidationResult(ValidationResultEvent event) {
    this.result = event;
  }
}
