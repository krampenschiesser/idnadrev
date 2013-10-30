package de.ks.application;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javafx.scene.Parent;

/**
 *
 */
public abstract class MainWindow {
  private Parent root;
  private String applicationTitle;

  public abstract Parent getRoot();

  public abstract String getApplicationTitle();

}
