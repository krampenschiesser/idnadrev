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

import javax.enterprise.inject.Specializes;
import javax.inject.Inject;
import java.util.LinkedList;

/**
 *
 */
@WorkflowScoped
@Specializes
public class WorkflowNavigator extends WorkflowConfig {
  @Inject
  protected WorkflowConfig cfg;

  protected LinkedList<WorkflowStepConfig> history = new LinkedList<>();
  protected ObjectProperty<WorkflowStepConfig> currentStep = new SimpleObjectProperty<>();

  public void start() {
    if (cfg.getStepList().isEmpty()) {
      throw new RuntimeException("No registered steps");
    }
    currentStep.set(cfg.getStepList().get(0));
  }

  public void next(String name) {
    WorkflowStepConfig old = getCurrentStep();
    WorkflowStepConfig next = old.getOutput(name);
    setCurrentStep(next);
    history.add(old);
  }


  public void next() {
    WorkflowStepConfig stepConfig = getCurrentStep();
    String output = stepConfig.getStep().getOutputValue();
    next(output);
  }

  public void previous() {
    WorkflowStepConfig last = history.removeLast();
    setCurrentStep(last);
  }

  public void cancel() {
    //
  }

  public ObjectProperty<WorkflowStepConfig> currentStepProperty() {
    return currentStep;
  }

  public WorkflowStepConfig getCurrentStep() {
    if (currentStep.get() == null) {
      start();
    }
    return currentStep.get();
  }

  public WorkflowNavigator setCurrentStep(WorkflowStepConfig step) {
    currentStep.set(step);
    return this;
  }
}
