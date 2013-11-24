package de.ks.workflow.cdi;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.Workflow;
import de.ks.workflow.step.EditStep;
import javafx.scene.Node;

/**
 *
 */
public class TestWorkflow2 extends Workflow<Object, Node, Object> {
  public static final String ID = TestWorkflow2.class.getName();

  @Override
  public Object getModel() {
    return new Object();
  }

  @Override
  public Class<Object> getModelClass() {
    return Object.class;
  }

  @Override
  protected void configureSteps() {
    cfg.startWith(EditStep.class);
  }
}
