package de.ks.application;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.collect.MapMaker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
public class Navigator {
  private static ConcurrentMap<Stage, Navigator> navigators = new MapMaker().weakKeys().makeMap();

  public static Navigator getNavigator(Stage stage) {
    return navigators.get(stage);
  }

  public static Navigator registerNavigator(Stage stage, Pane root) {
    Navigator navigator = new Navigator(root);
    navigators.putIfAbsent(stage, navigator);
    return navigator;
  }

  public static Navigator registerNavigatorWithBorderPane(Stage stage) {
    BorderPane borderPane = new BorderPane();
    StackPane content = new StackPane();
    borderPane.setCenter(content);
    Navigator navigator = new Navigator(content);

    content = new StackPane();
    borderPane.setTop(new StackPane());
    navigator.addPresentationArea(TOP_AREA, content);

    content = new StackPane();
    borderPane.setBottom(new StackPane());
    navigator.addPresentationArea(BOTTOM_AREA, content);

    content = new StackPane();
    borderPane.setLeft(new StackPane());
    navigator.addPresentationArea(LEFT_AREA, content);

    content = new StackPane();
    borderPane.setRight(new StackPane());
    navigator.addPresentationArea(RIGHT_AREA, content);

    navigators.put(stage, navigator);
    return navigator;
  }


  public static final String MAIN_AREA = "main";
  public static final String LEFT_AREA = "left";
  public static final String RIGHT_AREA = "right";
  public static final String TOP_AREA = "content";
  public static final String BOTTOM_AREA = "bottom";

  protected final ObservableMap<String, PresentationArea> presentationAreas = FXCollections.observableHashMap();
  protected final Map<String, Deque<Class<?>>> histories = new HashMap<>();


  private Navigator(Pane mainArea) {
    addPresentationArea(MAIN_AREA, mainArea);
  }

  public void addPresentationArea(String id, Pane pane) {
    PresentationArea presentationArea = new PresentationArea(id, pane);
    presentationArea.currentNodeProperty().addListener((observable, previous, current) -> {
      if (previous != null) {
//  histories.get(presentationArea.getId()).add()
      }
    });
    presentationAreas.put(id, presentationArea);
  }

  public void presentInMain(Node node) {
    present(MAIN_AREA, node);
  }

  public void present(String area, Node node) {
    presentationAreas.get(area).setCurrentNode(node);
  }

  public void back(String area) {

  }

  public void next(String area) {

  }

  public PresentationArea getPresentationArea(String id) {
    return presentationAreas.get(id);
  }

  public PresentationArea getMainArea() {
    return getPresentationArea(MAIN_AREA);
  }
}
