package de.ks.workflow.step;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.NodeProvider;
import javafx.scene.Node;

/**
 * @param <N> node implementation
 */
public abstract class InteractiveStep<N extends Node> extends WorkflowStep implements NodeProvider<N> {
}
