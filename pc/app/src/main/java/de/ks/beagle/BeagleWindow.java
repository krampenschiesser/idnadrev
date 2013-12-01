package de.ks.beagle;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.application.MainWindow;
import de.ks.application.fxml.DefaultLoader;
import de.ks.menu.presenter.MenuBarPresenter;
import de.ks.menu.sink.ContentSink;
import de.ks.menu.sink.PopupSink;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
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
  private DefaultLoader<BorderPane, Object> loader;

  @PostConstruct
  public void initialize() {
    popupSink.setMenuPath("/main/help");
    contentSink.setMenuPath("/main");
    loader = new DefaultLoader<>(BeagleWindow.class);
  }

  @Override
  public Parent getNode() {
    borderPane = loader.getView();
    borderPane.setTop(menuBarPresenter.getMenu("/main"));

    StackPane contentPane = new StackPane();
    borderPane.setCenter(contentPane);
    contentSink.setPane(contentPane);
    return borderPane;
  }

  @Override
  public String getApplicationTitle() {
    return "Beagle Version 0.3";
  }
}
