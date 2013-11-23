package de.ks.workflow.cdi;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.Workflow;

/**
 *
 */
public class TestWorkflow2 extends Workflow {
  public static final String ID = TestWorkflow2.class.getName();

  @Override
  public Object getModel() {
    return new Object();
  }

  @Override
  protected void configureSteps() {
  }
}
