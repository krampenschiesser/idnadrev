package de.ks.workflow.step;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javafx.scene.layout.GridPane;

/**
 *
 */
public class TableSelectionStep extends InteractiveStep<GridPane> {
  @Override
  public GridPane getNode() {
    return new GridPane();
  }
}
