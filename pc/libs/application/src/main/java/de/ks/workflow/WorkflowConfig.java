package de.ks.workflow;/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.cdi.WorkflowScoped;
import de.ks.workflow.step.DefaultOutput;
import de.ks.workflow.step.WorkflowStep;
import de.ks.workflow.step.WorkflowStepConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@WorkflowScoped
public class WorkflowConfig {
  private static final Logger log = LogManager.getLogger(WorkflowConfig.class);

  protected WorkflowStepConfig root;


  public WorkflowStepConfig startWith(Class<? extends WorkflowStep> step) {
    root = new WorkflowStepConfig(this, step, DefaultOutput.ROOT.name());
    return root;
  }

  public WorkflowStepConfig getRoot() {
    return root;
  }

  public List<WorkflowStepConfig> getStepList() {
    List<WorkflowStepConfig> retval = new ArrayList<>();

    for (WorkflowStepConfig cfg = root; cfg.hasOutput(DefaultOutput.NEXT.name()); cfg = cfg.getOutput(DefaultOutput.NEXT.name())) {
      retval.add(cfg);
    }

    return retval;
  }
}
