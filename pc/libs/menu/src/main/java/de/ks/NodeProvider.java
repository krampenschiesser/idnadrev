package de.ks;/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javafx.scene.Node;

public interface NodeProvider<T extends Node> {
  T getNode();
}
