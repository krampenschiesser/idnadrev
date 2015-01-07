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

package de.ks.application.fxml;

import de.ks.i18n.Localized;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @param <V> the view
 * @param <C> the controller
 */
public class DefaultLoader<V extends Node, C> {
  private static final Logger log = LoggerFactory.getLogger(DefaultLoader.class);
  private final Class<?> controller;
  private final URL fxmlFile;
  private FXMLLoader loader;
  protected final AtomicBoolean loaded = new AtomicBoolean(false);
  private ControllerFactory controllerFactory;
  private ResourceBundle resourceBundle;
  private C loadedInstance;

  public DefaultLoader(Class<?> modelController) {
    this(modelController, guessFxmlFile(modelController));
  }

  private static URL guessFxmlFile(Class<?> modelController) {
    String controllerName = modelController.getSimpleName();
    URL resource = modelController.getResource(controllerName + ".fxml");
    if (resource == null) {
      log.trace("Trying {}", controllerName + "View.fxml");
      resource = modelController.getResource(controllerName + "View.fxml");
    }
    if (resource == null) {
      if (controllerName.toLowerCase(Locale.ROOT).endsWith("controller")) {
        String substring = controllerName.substring(0, controllerName.length() - "controller".length());

        log.trace("Trying {}", substring + ".fxml");
        resource = modelController.getResource(substring + ".fxml");
        if (resource == null) {
          log.trace("Trying {}", substring + "View.fxml");
          resource = modelController.getResource(substring + "View.fxml");
        }
      }
    }
    return resource;
  }

  public DefaultLoader(URL fxmlFile) {
    this(null, fxmlFile);
  }

  protected DefaultLoader(Class<?> controller, URL fxmlFile) {
    this.controller = controller;
    this.fxmlFile = fxmlFile;
    if (fxmlFile == null && controller == null) {
      log.error("FXML file not found, is null!");
      throw new FXMLFileNotFoundException("FXML file not found, is null!");
    }
    controllerFactory = new ControllerFactory();
    resourceBundle = Localized.getBundle();
    loader = new FXMLLoader(fxmlFile, resourceBundle, new JavaFXBuilderFactory(), controllerFactory);
  }

  @SuppressWarnings("unchecked")
  public DefaultLoader<V, C> load() {
    try {
      if (loaded.compareAndSet(false, true)) {
        if (fxmlFile == null) {
          Object instance = controllerFactory.call(controller);
          if (instance instanceof Initializable) {
            ((Initializable) instance).initialize(null, resourceBundle);
          }
          this.loadedInstance = (C) instance;
        } else {
          log.debug("Loading fxml file {}", fxmlFile);
          Node loadedNode = loader.load();
          if (loadedNode != null) {
            fillLabelCopyMenu(loadedNode);
          }
          loaded.set(true);
        }
      }
    } catch (IOException e) {
      log.error("Could not load fxml file {}", fxmlFile, e);
      throw new RuntimeException(e);
    }
    return this;
  }

  protected void fillLabelCopyMenu(Node loadedNode) {
    LinkedList<Node> stack = new LinkedList<>();
    stack.add(loadedNode);

    while (!stack.isEmpty()) {
      Node node = stack.pollFirst();
      if (node instanceof Parent) {
        stack.addAll(((Parent) node).getChildrenUnmodifiable());
      }
      if (node instanceof TabPane) {
        ((TabPane) node).getTabs().forEach(t -> {
          log.info("Found tab pane, child node= {}", t.getContent());
          stack.add(t.getContent());
        });
      }
      if (node instanceof Accordion) {
        ((Accordion) node).getPanes().forEach(t -> stack.add(t.getContent()));
      }
      if (node instanceof SplitPane) {
        ((SplitPane) node).getItems().forEach(t -> stack.add(t));
      }
      if (node instanceof Label) {
        Label label = (Label) node;

        label.setOnMouseClicked(e -> {
          if (e.getButton() == MouseButton.SECONDARY) {
            MenuItem item = new MenuItem(Localized.get("copy"));
            ContextMenu contextMenu = new ContextMenu(item);
            item.setOnAction(actionEvent -> {
              HashMap<DataFormat, Object> content = new HashMap<>();
              content.put(DataFormat.PLAIN_TEXT, label.getText());
              Clipboard.getSystemClipboard().setContent(content);
            });

            contextMenu.show(label, e.getScreenX(), e.getScreenY());
          }
        });
      }
    }
  }

  public V getView() {
    if (fxmlFile == null) {
      return null;
    } else {
      return getLoader().getRoot();
    }
  }

  private FXMLLoader getLoader() {
    load();
    return loader;
  }

  public C getController() {
    if (fxmlFile == null) {
      load();
      return loadedInstance;
    } else {
      return getLoader().getController();
    }
  }

  public URL getFxmlFile() {
    return fxmlFile;
  }

  @Override
  public String toString() {
    return "DefaultLoader{" +
            "fxmlFile=" + fxmlFile +
            '}';
  }
}
