package de.ks.workflow.navigation;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.executor.ExecutorService;
import de.ks.workflow.WorkflowConfig;
import de.ks.workflow.cdi.WorkflowScoped;
import de.ks.workflow.step.AutomaticStep;
import de.ks.workflow.step.WorkflowStepConfig;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

import javax.enterprise.inject.Specializes;
import javax.inject.Inject;
import java.util.LinkedList;
import java.util.concurrent.Future;

/**
 *
 */
@WorkflowScoped
@Specializes
public class WorkflowNavigator extends WorkflowConfig {
  @Inject
  protected WorkflowConfig cfg;
  @Inject
  protected ExecutorService executor;

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

    if (next.getStep() instanceof AutomaticStep) {
      Task<String> task = ((AutomaticStep) next.getStep()).getTask();
      task.setOnSucceeded((event) -> next());

      Future<?> future = executor.submit(task);
    }
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
