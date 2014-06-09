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
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @param <V> the view
 * @param <C> the controller
 */
public class DefaultLoader<V extends Node, C> {
  private static final Logger log = LoggerFactory.getLogger(DefaultLoader.class);
  private final URL fxmlFile;
  private CompletableFuture<FXMLLoader> loaderFuture;
  private CompletableFuture<Void> allCallbacks;
  private FXMLLoader loader;
  protected final AtomicBoolean loaded = new AtomicBoolean(false);

  public DefaultLoader(Class<?> modelController) {
    this(modelController.getResource(modelController.getSimpleName() + ".fxml"));
  }

  public DefaultLoader(URL fxmlFile) {
    this.fxmlFile = fxmlFile;
    if (fxmlFile == null) {
      log.error("FXML file not found, is null!");
      throw new IllegalArgumentException("FXML file not found, is null!");
    }
    loader = new FXMLLoader(fxmlFile, Localized.getBundle(), new JavaFXBuilderFactory(), new ControllerFactory());
  }

  public FXMLLoader load() {
    try {
      if (loaded.compareAndSet(false, true)) {
        log.debug("Loading fxml file {}", fxmlFile);
        loader.load();
        loaded.set(true);
      }
    } catch (IOException e) {
      log.error("Could not load fxml file {}", fxmlFile, e);
      throw new RuntimeException(e);
    }
    return loader;
  }

  public V getView() {
    return getLoader().getRoot();
  }

  private FXMLLoader getLoader() {
    load();
    return loader;
  }

  public C getController() {
    return getLoader().getController();
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
