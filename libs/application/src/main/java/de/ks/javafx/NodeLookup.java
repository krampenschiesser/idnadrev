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
package de.ks.javafx;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NodeLookup {

  public static Set<Node> getAllIdNodes(Node node) {
    return getAllNodes(node, m -> m.getId() != null);
  }

  public static Set<Node> getAllNodes(Node node, Predicate<Node> filter) {
    Set<Node> allNodes = new LinkedHashSet<>();
    if (filter.test(node)) {
      allNodes.add(node);
    }

    if (node instanceof Parent) {
      ObservableList<Node> childrenUnmodifiable = ((Parent) node).getChildrenUnmodifiable();

      List<Node> idNodes = childrenUnmodifiable.stream().filter(filter).collect(Collectors.toList());
      allNodes.addAll(idNodes);
      for (Node child : childrenUnmodifiable) {
        allNodes.addAll(getAllNodes(child, filter));
      }
    }
    if (node instanceof TabPane) {
      ObservableList<Tab> tabs = ((TabPane) node).getTabs();
      for (Tab tab : tabs) {
        allNodes.addAll(getAllNodes(tab.getContent(), filter));
      }
    }
    return allNodes;
  }

  public static Node getChildNodeWithId(Node node, String name) {
    List<Node> collect = getAllIdNodes(node).stream().filter((n) -> n.getId() != null && n.getId().equals(name)).collect(Collectors.toList());
    if (collect.size() > 1) {
      throw new IllegalStateException("Found " + collect.size() + " nodes with id='" + name + "'");
    } else if (collect.size() == 1) {
      return collect.get(0);
    } else {
      return null;
    }
  }
}
