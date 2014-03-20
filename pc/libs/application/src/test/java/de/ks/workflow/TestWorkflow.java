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

import de.ks.workflow.cdi.WorkflowScoped;
import de.ks.workflow.cdi.WorkflowSpecificLiteral;
import de.ks.workflow.step.EditStep;
import de.ks.workflow.step.PersistStep;
import de.ks.workflow.step.TableSelectionStep;
import de.ks.workflow.step.WorkflowStepConfig;
import de.ks.workflow.view.full.FullWorkflowView;
import javafx.scene.layout.BorderPane;

@WorkflowScoped
public class TestWorkflow extends Workflow<SimpleWorkflowModel, BorderPane, FullWorkflowView> {
  private SimpleWorkflowModel model = new SimpleWorkflowModel();

  public static WorkflowSpecificLiteral getLiteral() {
    return new WorkflowSpecificLiteral(TestWorkflow.class);
  }

  @Override
  public SimpleWorkflowModel getModel() {
    return model;
  }

  @Override
  public Class<SimpleWorkflowModel> getModelClass() {
    return SimpleWorkflowModel.class;
  }

  @Override
  protected void configureSteps() {
    WorkflowStepConfig root = cfg.startWith(EditStep.class);
    WorkflowStepConfig persistStep = root.next(PersistStep.class);
    persistStep.error(root);

    WorkflowStepConfig selection = persistStep.next(TableSelectionStep.class);
    WorkflowStepConfig background = selection.next(PersistStep.class);

    background.branch("first", EditStep.class).next(PersistStep.class).restartWithNext();
    background.branch("second", EditStep.class).next(PersistStep.class).restartWithNext();
  }

}
