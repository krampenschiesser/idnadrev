package de.ks.workflow.view.step;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.WorkflowNavigator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 */
public class HorizontalNextPreviousView implements Initializable {
  @FXML
  protected Button previous;
  @FXML
  protected Button next;
  @FXML
  protected Button cancel;
  @Inject
  protected WorkflowNavigator navigator;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    next.setOnAction((event) -> navigator.next());
    previous.setOnAction((event) -> navigator.previous());
    cancel.setOnAction((event) -> navigator.cancel());
  }

  public Button getPrevious() {
    return previous;
  }

  public Button getNext() {
    return next;
  }

  public Button getCancel() {
    return cancel;
  }

}