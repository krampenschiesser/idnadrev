package de.ks.application;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class PresentationArea {
  private static final Logger log = LoggerFactory.getLogger(PresentationArea.class);
  protected final String id;
  protected final Pane content;
  protected final SimpleObjectProperty<Node> currentNode = new SimpleObjectProperty<>();

  public PresentationArea(String id, Pane content) {
    this.id = id;
    this.content = content;
    currentNode.addListener((o, f, n) -> {
      log.debug("Showing new node {} in content {} of presentation area {}", n, content, this);
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

  public SimpleObjectProperty<Node> currentNodeProperty() {
    return currentNode;
  }

  @Override
  public String toString() {
    return super.toString() + " {" +
            "id='" + id + '\'' +
            '}';
  }
}
