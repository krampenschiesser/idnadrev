package de.ks.workflow.view.full;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.WorkflowNavigator;
import de.ks.workflow.step.InteractiveStep;
import de.ks.workflow.step.WorkflowStep;
import de.ks.workflow.view.navigator.ListBasedStepView;
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
  ListBasedStepView stepViewController;
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

  public ListBasedStepView getStepViewController() {
    return stepViewController;
  }

  public HorizontalNextPreviousView getStepButtonController() {
    return stepButtonController;
  }

  public StackPane getContent() {
    return content;
  }
}
