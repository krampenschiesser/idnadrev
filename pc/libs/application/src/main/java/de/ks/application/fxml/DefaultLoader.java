package de.ks.application.fxml;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.executor.ExecutorService;
import de.ks.i18n.Localized;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @param <V> the view
 * @param <C> the controller
 */
public class DefaultLoader<V extends Node, C> {
  private static final Logger log = LogManager.getLogger(DefaultLoader.class);
  private final FXMLLoader loader;
  private final Future<V> future;

  public DefaultLoader(Class<?> modelController) {
    this(modelController.getResource(modelController.getSimpleName() + ".fxml"));
  }

  public DefaultLoader(URL fxmlFile) {
    if (fxmlFile == null) {
      log.error("FXML file not found, is null!");
      throw new IllegalArgumentException("FXML file not found, is null!");
    }
    loader = new FXMLLoader(fxmlFile, Localized.getBundle(), new JavaFXBuilderFactory(), new ControllerFactory());
    future = ExecutorService.instance.executeInJavaFXThread((Callable<V>) () -> loader.load());
  }


  public V getView() {
    try {
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Could not load given FXML file " + loader.getLocation(), e);
      throw new RuntimeException(e);
    }
  }

  public C getController() {
    if (!future.isDone()) {
      getView();//load it
    }
    return loader.getController();
  }
}
