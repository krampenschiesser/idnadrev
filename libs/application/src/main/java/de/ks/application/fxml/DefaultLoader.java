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

package de.ks.application.fxml;


import de.ks.executor.JavaFXExecutorService;
import de.ks.i18n.Localized;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.fxml.LoadException;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @param <V> the view
 * @param <C> the controller
 */
public class DefaultLoader<V extends Node, C> {
  private static final Logger log = LoggerFactory.getLogger(DefaultLoader.class);
  private final ExecutorService service;
  private final URL fxmlFile;
  private CompletableFuture<FXMLLoader> loaderFuture;
  private CompletableFuture<Void> allCallbacks;

  public DefaultLoader(Class<?> modelController) {
    this(modelController, null);
  }

  public DefaultLoader(URL fxmlFile) {
    this(fxmlFile, null);
  }

  public DefaultLoader(Class<?> modelController, ExecutorService executor) {
    this(modelController.getResource(modelController.getSimpleName() + ".fxml"), executor);
  }

  public DefaultLoader(URL fxmlFile, ExecutorService executor) {
    if (executor == null) {
      service = CDI.current().select(de.ks.executor.ExecutorService.class).get();
    } else {
      service = executor;
    }

    this.fxmlFile = fxmlFile;
    if (fxmlFile == null) {
      log.error("FXML file not found, is null!");
      throw new IllegalArgumentException("FXML file not found, is null!");
    }

    Supplier<FXMLLoader> supplier = () -> {
      try {
        FXMLLoader loader = new FXMLLoader(fxmlFile, Localized.getBundle(), new JavaFXBuilderFactory(), new ControllerFactory());
        log.debug("Loading fxml file {}", fxmlFile);
        loader.load();
        return loader;
      } catch (IOException e) {
        log.error("Could not load fxml file {}", fxmlFile, e);
        throw new RuntimeException(e);
      }
    };
    loaderFuture = CompletableFuture.supplyAsync(supplier, service).exceptionally((t) -> {
      if (t.getCause() instanceof RuntimeException && t.getCause().getCause() instanceof LoadException) {
        log.info("Last load of {} failed, will try again in JavaFX Thread", fxmlFile);
        return new JavaFXExecutorService().invokeInJavaFXThread(() -> supplier.get());
      }
      throw new RuntimeException(t);
    });
  }

  public void addCallback(BiConsumer<Object, Node> callback) {
    CompletableFuture<Object> controllerFuture = loaderFuture.thenApply(FXMLLoader::getController);
    CompletableFuture<Node> nodeFuture = loaderFuture.thenApply(FXMLLoader::getRoot);

    CompletableFuture<Void> asyncCall = controllerFuture.thenAcceptBothAsync(nodeFuture, callback, service);
    asyncCall.thenRun(() -> log.trace("Done with {} for {}", callback, this.fxmlFile));
    if (allCallbacks == null) {
      allCallbacks = asyncCall;
    } else {
      allCallbacks = CompletableFuture.allOf(allCallbacks, asyncCall);
    }
  }

  public V getView() {
    waitForLoading();
    return getLoader().getRoot();
  }

  private FXMLLoader getLoader() {
    try {
      return loaderFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  public void waitForLoading() {
    if (allCallbacks == null) {
      log.trace("Waiting simple fxml loading");
      loaderFuture.join();
    } else {
      log.trace("Waiting for all callbacks {}", allCallbacks);
      allCallbacks.join();
    }
  }

  public boolean isLoaded() {
    return allCallbacks.isDone();
  }

  public C getController() {
    waitForLoading();
    return getLoader().getController();
  }
}
