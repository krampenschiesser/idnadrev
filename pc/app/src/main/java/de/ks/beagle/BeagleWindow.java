package de.ks.beagle;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.application.MainWindow;
import de.ks.menu.presenter.MenuBarPresenter;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import javax.inject.Inject;

/**
 *
 */
public class BeagleWindow extends MainWindow {
  @Inject
  MenuBarPresenter menuBarPresenter;

  @Override
  public Parent getRoot() {
    BorderPane borderPane = new BorderPane();
    borderPane.setPrefSize(640, 480);
    borderPane.setTop(menuBarPresenter.getMenu("/main"));
    return borderPane;
  }

  @Override
  public String getApplicationTitle() {
    return "Beagle Version 0.3";
  }
}
