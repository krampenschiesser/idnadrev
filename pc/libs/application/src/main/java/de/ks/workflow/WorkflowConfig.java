package de.ks.workflow;/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.cdi.WorkflowScoped;
import de.ks.workflow.step.DefaultOutput;
import de.ks.workflow.step.WorkflowStep;
import de.ks.workflow.step.WorkflowStepConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@WorkflowScoped
public class WorkflowConfig {
  private static final Logger log = LoggerFactory.getLogger(WorkflowConfig.class);

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
    if (retval.isEmpty()) {
      retval.add(root);
    }

    return retval;
  }
}
