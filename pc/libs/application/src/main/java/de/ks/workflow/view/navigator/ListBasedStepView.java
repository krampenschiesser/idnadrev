package de.ks.workflow.view.navigator;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.cdi.WorkflowScoped;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

@WorkflowScoped
public class ListBasedStepView {
  @FXML
  ListView stepList;


}