package de.ks.activity.callback;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.reflection.ReflectionUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiConsumer;

/**
 *
 */
public abstract class LoaderCallback implements BiConsumer<Object, Node> {
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
      log.error("Could not do something", e);
    }
  }
}
