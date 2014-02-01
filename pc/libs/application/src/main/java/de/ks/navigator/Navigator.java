package de.ks.navigator;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.Workflow;
import de.ks.workflow.cdi.WorkflowContext;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 */

public class Navigator {
  protected ConcurrentLinkedQueue<Class<?>> workflows = new ConcurrentLinkedQueue<>();
  protected Scene scene;
  protected Class<? extends Workflow> homeWorkflow;
  protected Pane presentationPane;


  public Navigator setPresentationPane(Pane pane) {
    this.presentationPane = pane;
    return this;
  }

  public void home() {
    WorkflowContext.start(homeWorkflow);
  }

  public void back() {

  }

  public void next(Class<?> workflow) {

  }
}
