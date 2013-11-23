package de.ks.application;/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.NodeProvider;
import javafx.scene.Node;

/**
 * A provider for simple editor grids(2 columns).
 * Besides the editor part(which is resolved via {@link de.ks.NodeProvider#getNode()}
 * it also provides the descriptor, which is usually a label, positioned in the first/left column.
 */
public interface Grid2DEditorProvider<D extends Node, N extends Node> extends NodeProvider<N> {
  /**
   * @return the right (editor) for the 2 column grid
   */
  @Override
  N getNode();

  /**
   * @return the left (usually a label) for the 2 column grid
   */
  D getDescriptor();
}
