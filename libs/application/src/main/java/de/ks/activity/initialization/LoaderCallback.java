/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
 *
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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

/**
 *
 */
public abstract class LoaderCallback {
  private static final Logger log = LoggerFactory.getLogger(LoaderCallback.class);

  protected void addHandlerToNode(Node node, String id, EventHandler<ActionEvent> handler) {
    try {
      if (!id.startsWith("#")) {
        id = "#" + id;
      }
      Node found = node.lookup(id);
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

  public abstract void accept(Object controller, Node node);

  public abstract void doInFXThread(Object controller, Node node);

}
