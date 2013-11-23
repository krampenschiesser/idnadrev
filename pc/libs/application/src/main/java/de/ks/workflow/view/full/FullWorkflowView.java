package de.ks.workflow.view.full;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.cdi.WorkflowScoped;
import de.ks.workflow.view.navigator.ListBasedStepView;
import de.ks.workflow.view.step.HorizontalNextPreviousView;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

/**
 *
 */
@WorkflowScoped
public class FullWorkflowView {
  @FXML
  StackPane content;
  @FXML
  ListBasedStepView stepViewController;
  @FXML
  HorizontalNextPreviousView stepButtonController;
}
