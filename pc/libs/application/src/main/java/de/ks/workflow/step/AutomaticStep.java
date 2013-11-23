package de.ks.workflow.step;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import java.util.concurrent.Callable;

/**
 *
 */
public abstract class AutomaticStep extends WorkflowStep implements Callable<String> {
}
