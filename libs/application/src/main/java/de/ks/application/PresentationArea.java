/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ks.application;

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
