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


import de.ks.workflow.Workflow;
import de.ks.workflow.step.EditStep;
import javafx.scene.Node;

/**
 *
 */
public class TestWorkflow1 extends Workflow<Object, Node, Object> {
  public static final String ID = TestWorkflow1.class.getName();

  @Override
  public Object getModel() {
    return new Object();
  }

  @Override
  public Class<Object> getModelClass() {
    return Object.class;
  }

  @Override
  protected void configureSteps() {
    cfg.startWith(EditStep.class);
  }
}
