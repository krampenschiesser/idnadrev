/*
 * Copyright [2014] [Christian Loehnert]
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

package de.ks.beagle.workflow.addthought;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.beagle.entity.Thought;
import de.ks.menu.MenuItem;
import de.ks.workflow.Workflow;
import de.ks.workflow.cdi.WorkflowScoped;
import de.ks.workflow.step.EditStep;
import de.ks.workflow.step.PersistStep;
import de.ks.workflow.view.full.FullWorkflowView;
import javafx.scene.layout.BorderPane;

@WorkflowScoped
@MenuItem("/main/thought")
public class AddThoughtWorkflow extends Workflow<Thought, BorderPane, FullWorkflowView> {
  protected Thought model = new Thought();

  @Override
  public Thought getModel() {
    return model;
  }

  @Override
  public Class<Thought> getModelClass() {
    return Thought.class;
  }

  @Override
  protected void configureSteps() {
    cfg.startWith(EditStep.class).next(PersistStep.class);
  }
}
