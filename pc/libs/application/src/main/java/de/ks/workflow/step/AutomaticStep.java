package de.ks.workflow.step;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javafx.concurrent.Task;

/**
 *
 */
public abstract class AutomaticStep extends WorkflowStep {
  public abstract Task<String> getTask();

}
