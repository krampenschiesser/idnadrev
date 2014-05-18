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


import com.google.common.eventbus.Subscribe;
import de.ks.LauncherRunner;
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
@Ignore
@RunWith(LauncherRunner.class)
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
    Thread.sleep(100);
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
