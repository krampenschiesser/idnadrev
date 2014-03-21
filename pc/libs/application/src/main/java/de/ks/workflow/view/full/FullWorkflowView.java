/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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

package de.ks.workflow.view.full;


import de.ks.workflow.navigation.WorkflowNavigator;
import de.ks.workflow.step.InteractiveStep;
import de.ks.workflow.step.WorkflowStep;
import de.ks.workflow.view.step.HorizontalNextPreviousView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 */
public class FullWorkflowView implements Initializable {
  @FXML
  StackPane content;
  @FXML
  StackPane stepView;
  @FXML
  StackPane stepButton;
  @FXML
  HorizontalNextPreviousView stepButtonController;

  @Inject
  WorkflowNavigator navigator;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    navigator.currentStepProperty().addListener((observable, old, current) -> {
      layoutNextStep(current.getStep());
    });

    layoutNextStep(navigator.getCurrentStep().getStep());
  }

  protected void layoutNextStep(WorkflowStep step) {
    if (step instanceof InteractiveStep) {
      Node node = ((InteractiveStep) step).getNode();
      content.getChildren().clear();
      content.getChildren().add(node);
    }
  }

  public HorizontalNextPreviousView getStepButtonController() {
    return stepButtonController;
  }

  public StackPane getContent() {
    return content;
  }
}
