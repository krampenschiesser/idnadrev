package de.ks.beagle;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.menu.MenuItem;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 *
 */
@MenuItem("/main/thought")
public class AddThought extends VBox {
  public AddThought() {
    getChildren().addAll(new Label("Yeah!"));
  }
}
