package de.ks.application;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 *
 */
public class PresentationArea {
  protected final String id;
  protected final Pane content;
  protected final SimpleObjectProperty<Node> currentNode = new SimpleObjectProperty<>();

  public PresentationArea(String id, Pane content) {
    this.id = id;
    this.content = content;
    currentNode.addListener((o, f, n) -> {
      content.getChildren().clear();

      content.getChildren().add(n);
    });
  }

  public String getId() {
    return id;
  }

  public Pane getContent() {
    return content;
  }

  public Node getCurrentNode() {
    return currentNode.get();
  }

  public PresentationArea setCurrentNode(Node node) {
    currentNode.set(node);
    return this;
  }

  public  SimpleObjectProperty<Node> currentNodeProperty() {
    return currentNode;
  }
}
