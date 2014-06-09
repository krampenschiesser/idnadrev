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

package de.ks.activity.initialization;

import de.ks.reflection.ReflectionUtil;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public abstract class LoaderCallback {
  private static final Logger log = LoggerFactory.getLogger(LoaderCallback.class);

  protected void addHandlerToNode(Node node, String id, EventHandler<ActionEvent> handler) {
    try {
      Node found = getChildNodeWithId(node, id);
      List<Method> methods = ReflectionUtil.getAllMethods(found.getClass(), (m) -> {
        return m.getName().equals("setOnAction") && m.getParameterTypes().length == 1 && EventHandler.class.isAssignableFrom(m.getParameterTypes()[0]);
      });
      if (methods.size() == 1) {
        Method method = methods.get(0);
        ReflectionUtil.invokeMethod(method, found, handler);
      }
    } catch (Exception e) {
      log.error("Could execute loader callback", e);
    }
  }

  protected Set<Node> getAllIdNodes(Node node) {
    Set<Node> allNodes = new LinkedHashSet<>();
    if (node.getId() != null) {
      allNodes.add(node);
    }

    if (node instanceof Parent) {
      ObservableList<Node> childrenUnmodifiable = ((Parent) node).getChildrenUnmodifiable();

      List<Node> idNodes = childrenUnmodifiable.stream().filter((n) -> n.getId() != null).collect(Collectors.toList());
      allNodes.addAll(idNodes);
      for (Node child : childrenUnmodifiable) {
        allNodes.addAll(getAllIdNodes(child));
      }
    }
    if (node instanceof TabPane) {
      ObservableList<Tab> tabs = ((TabPane) node).getTabs();
      for (Tab tab : tabs) {
        allNodes.addAll(getAllIdNodes(tab.getContent()));
      }
    }
    return allNodes;
  }

  protected Node getChildNodeWithId(Node node, String name) {
    List<Node> collect = getAllIdNodes(node).stream().filter((n) -> n.getId() != null && n.getId().equals(name)).collect(Collectors.toList());
    if (collect.size() > 1) {
      throw new IllegalStateException("Foudn " + collect.size() + " nodes with id='" + name + "'");
    } else if (collect.size() == 1) {
      return collect.get(0);
    } else {
      return null;
    }
  }

  public abstract void accept(Object controller, Node node);

  public abstract void doInFXThread(Object controller, Node node);

}
