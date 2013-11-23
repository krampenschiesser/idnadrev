package de.ks.beagle;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.application.MainWindow;
import de.ks.menu.presenter.MenuBarPresenter;
import de.ks.menu.sink.ContentSink;
import de.ks.menu.sink.PopupSink;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 *
 */
public class BeagleWindow extends MainWindow {
  private static final Logger log = LogManager.getLogger(BeagleWindow.class);

  @Inject
  MenuBarPresenter menuBarPresenter;
  @Inject
  PopupSink popupSink;
  @Inject
  ContentSink contentSink;

  private BorderPane borderPane;

  @PostConstruct
  public void initialize() {
    popupSink.setMenuPath("/main/help");
    contentSink.setMenuPath("/main");
  }

  @Override
  public Parent getNode() {
    borderPane = new BorderPane();
    borderPane.setPrefSize(640, 480);
    borderPane.setTop(menuBarPresenter.getMenu("/main"));

    VBox vBox = new VBox();
    borderPane.setCenter(vBox);
    contentSink.setPane(vBox);
    return borderPane;
  }

  @Override
  public String getApplicationTitle() {
    return "Beagle Version 0.3";
  }
}
