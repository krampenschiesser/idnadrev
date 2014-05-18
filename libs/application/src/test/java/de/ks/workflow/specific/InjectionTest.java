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

package de.ks.workflow.specific;


import de.ks.LauncherRunner;
import de.ks.workflow.cdi.TestWorkflow1;
import de.ks.workflow.cdi.TestWorkflow2;
import de.ks.workflow.cdi.WorkflowSpecificLiteral;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static org.junit.Assert.assertFalse;

@RunWith(LauncherRunner.class)
public class InjectionTest {

  @Inject
  @Any
  Instance<Object> provider;

  @Test
  public void testInheritanceSpecialization() {
    assertFalse(provider.select(TestBean.class, new DefaultLiteral()).isUnsatisfied());

    assertFalse(provider.select(TestBean.class, new WorkflowSpecificLiteral(TestWorkflow1.class)).isUnsatisfied());
    assertFalse(provider.select(TestBean.class, new WorkflowSpecificLiteral(TestWorkflow2.class)).isUnsatisfied());
  }
}
