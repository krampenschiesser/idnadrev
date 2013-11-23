package de.ks.workflow.view.step;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.cdi.WorkflowScoped;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 *
 */
@WorkflowScoped
public class HorizontalNextPreviousView {
  @FXML
  protected Button previous;
  @FXML
  protected Button next;
  @FXML
  protected Button cancel;


}
