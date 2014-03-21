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


import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.ks.executor.ExecutorService;
import de.ks.i18n.Localized;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

/**
 * @param <V> the view
 * @param <C> the controller
 */
public class DefaultLoader<V extends Node, C> {
  private static final Logger log = LoggerFactory.getLogger(DefaultLoader.class);
  private final Phaser phaser = new Phaser();
  private final FXMLLoader loader;
  private final ListenableFuture<V> future;

  public DefaultLoader(Class<?> modelController) {
    this(modelController.getResource(modelController.getSimpleName() + ".fxml"));
  }

  public DefaultLoader(URL fxmlFile) {
    if (fxmlFile == null) {
      log.error("FXML file not found, is null!");
      throw new IllegalArgumentException("FXML file not found, is null!");
    }
    log.debug("Loading fxml file {}", fxmlFile);
    loader = new FXMLLoader(fxmlFile, Localized.getBundle(), new JavaFXBuilderFactory(), new ControllerFactory());

    phaser.register();
    future = ExecutorService.instance.executeInJavaFXThread((Callable<V>) () -> {
      try {
        return loader.load();
      } finally {
        phaser.arriveAndDeregister();
      }
    });
  }

  public void addCallback(BiConsumer<C, V> callback) {
    phaser.register();
    Futures.addCallback(future, new FutureCallback<V>() {
      @Override
      public void onSuccess(V result) {
        try {
          C controller = loader.getController();
          callback.accept(controller, result);
        } finally {
          phaser.arriveAndDeregister();
        }
      }

      @Override
      public void onFailure(Throwable t) {
        phaser.arriveAndDeregister();
      }
    }, ExecutorService.instance);
  }

  public V getView() {
    try {
      waitForAllParties();
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Could not load given FXML file " + loader.getLocation(), e);
      throw new RuntimeException(e);
    }
  }

  private void waitForAllParties() {
    phaser.register();
    int phase = -1;
    try {
      phase = phaser.arriveAndDeregister();
      phaser.awaitAdvanceInterruptibly(phase, 5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      log.warn("Got interrupted during arrival");
    } catch (TimeoutException e) {
      String msg = "Did not return from phase " + phase;
      log.error(msg);
      throw new RuntimeException(msg);
    }
  }

  public C getController() {
    if (!future.isDone()) {
      getView();//load it
    }
    return loader.getController();
  }
}
