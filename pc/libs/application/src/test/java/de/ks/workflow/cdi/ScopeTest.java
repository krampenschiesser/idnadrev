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

package de.ks.workflow.cdi;

/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.JFXCDIRunner;
import de.ks.executor.ExecutorService;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import static org.junit.Assert.*;

@RunWith(JFXCDIRunner.class)
public class ScopeTest {
  @Inject
  WorkflowScopedBean1 bean1;
  @Inject
  WorkflowScopedBean1 bean2;
  @Inject
  WorkflowContext context;

  @After
  public void tearDown() throws Exception {
    WorkflowContext.stopAll();
  }

  @Test
  public void testWorkflowContextActive() throws Exception {
    WorkflowContext context = (WorkflowContext) CDI.current().getBeanManager().getContext(WorkflowScoped.class);
    assertTrue(context.isActive());

    context.startWorkflow(TestWorkflow1.class).getCount();

    context.getWorkflow().waitForInitialization();
    bean1.getName();

    context.startWorkflow(TestWorkflow2.class);
    bean2.getName();
    assertEquals(2, context.workflows.size());

    context.getWorkflow().waitForInitialization();

    context.stopWorkflow(TestWorkflow1.class);
    assertEquals(1, context.workflows.size());
    assertNull(context.workflows.get(TestWorkflow1.class));
    assertFalse(context.workflows.get(TestWorkflow2.class).getObjectStore().isEmpty());

    context.cleanupAllWorkflows();
    assertEquals(0, context.workflows.size());
  }

  @Test
  public void testScopePropagation() throws Exception {
    WorkflowContext.start(TestWorkflow2.class);

    bean1.setValue("Hello Sauerland!");

    ExecutorService.instance.invokeAndWait(() -> {
      WorkflowScopedBean1 bean = CDI.current().select(WorkflowScopedBean1.class).get();
      assertNotNull(bean.getValue());
      assertEquals("Hello Sauerland!", bean.getValue());
    });
    assertEquals("Hello Sauerland!", bean1.getValue());
  }

  @Test
  public void testScopeCleanup() throws Exception {
    final CyclicBarrier barrier = new CyclicBarrier(2);
    WorkflowContext.start(TestWorkflow2.class);

    bean1.setValue("Hello Sauerland!");

    ExecutorService.instance.execute(() -> {
      WorkflowScopedBean1 bean = CDI.current().select(WorkflowScopedBean1.class).get();
      assertNotNull(bean.getValue());
      assertEquals("Hello Sauerland!", bean.getValue());
      try {
        barrier.await();
      } catch (InterruptedException | BrokenBarrierException e) {
        //brz
      }
    });
    assertEquals(1, context.workflows.size());
    assertEquals("Hello Sauerland!", bean1.getValue());
    WorkflowContext.stop(TestWorkflow2.class);

    assertEquals("Workflow got removed although one thread still holds it", 1, context.workflows.size());
    barrier.await();
    Thread.sleep(500);

    assertEquals(0, context.workflows.size());
  }
}
