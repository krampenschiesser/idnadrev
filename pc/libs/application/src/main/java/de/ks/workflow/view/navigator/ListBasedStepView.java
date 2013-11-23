package de.ks.workflow.view.navigator;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.WorkflowConfig;
import de.ks.workflow.WorkflowNavigator;
import de.ks.workflow.step.WorkflowStepConfig;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ListBasedStepView implements Initializable {
  @FXML
  ListView<String> stepList;
  @Inject
  WorkflowNavigator stepState;
  @Inject
  WorkflowConfig cfg;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    ObservableList<String> items = stepList.getItems();

    List<WorkflowStepConfig> stepList1 = cfg.getStepList();
    for (WorkflowStepConfig workflowStepConfig : stepList1) {
      items.add(workflowStepConfig.getStep().getTitle());
    }
    stepState.currentStepProperty().addListener((o, old, current) -> {
      if (current != null) {
        stepList.getSelectionModel().select(current.getStep().getTitle());
      }
    });
  }

  public ListView getStepList() {
    return stepList;
  }
}