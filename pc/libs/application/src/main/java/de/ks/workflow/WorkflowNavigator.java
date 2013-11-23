package de.ks.workflow;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.cdi.WorkflowScoped;
import de.ks.workflow.step.WorkflowStepConfig;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
@WorkflowScoped
public class WorkflowNavigator {
  @Inject
  WorkflowConfig cfg;

  protected List<WorkflowStepConfig> history = new LinkedList<>();
  protected ObjectProperty<WorkflowStepConfig> currentStep = new SimpleObjectProperty<>();

  public void start() {
    currentStep.set(cfg.getStepList().get(0));
  }

  public void next(String name) {
    WorkflowStepConfig next = currentStep.get().getOutput(name);
    currentStep.set(next);
  }

  public ObjectProperty<WorkflowStepConfig> currentStepProperty() {
    return currentStep;
  }

}
