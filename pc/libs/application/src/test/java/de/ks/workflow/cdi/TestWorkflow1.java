package de.ks.workflow.cdi;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.Workflow;
import javafx.scene.Node;

/**
 *
 */
public class TestWorkflow1 extends Workflow<Object, Node, Object> {
  public static final String ID = TestWorkflow1.class.getName();

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
  }
}
