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

import de.ks.activity.ActivityCfg;
import de.ks.activity.ActivityController;
import de.ks.activity.context.ActivityScoped;
import de.ks.application.fxml.DefaultLoader;
import de.ks.eventsystem.bus.EventBus;
import de.ks.executor.JavaFXExecutorService;
import javafx.fxml.LoadException;
import javafx.scene.Node;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ActivityScoped
public class ActivityInitialization {
  private static final Logger log = LoggerFactory.getLogger(ActivityInitialization.class);

  protected final ConcurrentHashMap<Class<?>, LinkedList<Pair<Object, Node>>> controllers = new ConcurrentHashMap<>();
  protected final Map<Class<?>, CompletableFuture<DefaultLoader<Node, Object>>> preloads = new HashMap<>();
  protected final ThreadLocal<List<Object>> currentlyLoadedControllers = ThreadLocal.withInitial(ArrayList::new);
  protected final List<DatasourceCallback> dataStoreCallbacks = new ArrayList<>();
  protected final List<ActivityCallback> activityCallbacks = new ArrayList<>();

  @Inject
  ActivityController controller;
  @Inject
  EventBus eventBus;

  public void loadActivity(ActivityCfg activityCfg) {
    currentlyLoadedControllers.get().clear();
    loadControllers(activityCfg);
    initalizeControllers();
  }

  protected void loadControllers(ActivityCfg activityCfg) {
    loadController(activityCfg.getInitialController());
    activityCfg.getAdditionalControllers().forEach(this::loadController);
    preloads.values().forEach(CompletableFuture::join);
  }

  private boolean shouldLoadInFXThread(Class<?> clazz) {
    return clazz.isAnnotationPresent(LoadInFXThread.class);
  }

  @SuppressWarnings("unchecked")
  public <T> DefaultLoader<Node, T> loadAdditionalController(Class<T> controllerClass) {
    DefaultLoader<Node, T> loader = new DefaultLoader<>(controllerClass);

    if (shouldLoadInFXThread(controllerClass)) {
      loader = controller.getJavaFXExecutor().invokeInJavaFXThread(loader::load);
      log.debug("Loaded additional controller {} in fx thread", controllerClass);
    } else {
      loader.load();
      log.info("Loaded additional controller {} in current thread", controllerClass);
    }
    currentlyLoadedControllers.get().add(loader.getController());
    return loader;
  }

  public <T> CompletableFuture<DefaultLoader<Node, T>> loadAdditionalControllerWithFuture(Class<T> controllerClass) {
    @SuppressWarnings("unchecked") DefaultLoader<Node, T> loader = loadAdditionalController(controllerClass);
    CompletableFuture completed = CompletableFuture.completedFuture(loader);
    return completed;
  }

  private void loadController(Class<?> controllerClass) {
    JavaFXExecutorService javaFXExecutor = controller.getJavaFXExecutor();
    ExecutorService executorService;
    if (shouldLoadInFXThread(controllerClass)) {
      executorService = javaFXExecutor;
    } else {
      executorService = controller.getExecutorService();
    }

    if (!preloads.containsKey(controllerClass)) {
      CompletableFuture<DefaultLoader<Node, Object>> loaderFuture = CompletableFuture.supplyAsync(getDefaultLoaderSupplier(controllerClass), executorService).exceptionally((t) -> {
        if (t.getCause() instanceof RuntimeException && t.getCause().getCause() instanceof LoadException) {
          currentlyLoadedControllers.get().forEach(eventBus::unregister);
          currentlyLoadedControllers.get().clear();
          log.info("Last load of {} failed, will try again in JavaFX Thread", new DefaultLoader<>(controllerClass).getFxmlFile());
          return javaFXExecutor.invokeInJavaFXThread(() -> getDefaultLoaderSupplier(controllerClass).get());
        }
        throw new RuntimeException(t);
      });

      preloads.put(controllerClass, loaderFuture);
    }
  }

  private Supplier<DefaultLoader<Node, Object>> getDefaultLoaderSupplier(Class<?> controllerClass) {
    return () -> {
      DefaultLoader<Node, Object> loader = new DefaultLoader<>(controllerClass);
      loader.load();
      Node view = loader.getView();

      currentlyLoadedControllers.get().forEach((c) -> {
        assert c != null;
        log.debug("Registering controller {} with node {}", c, view);
        controllers.putIfAbsent(c.getClass(), new LinkedList<>());
        controllers.get(c.getClass()).add(Pair.of(c, view));
      });
      currentlyLoadedControllers.get().clear();
      return loader;
    };
  }

  public void addControllerToInitialize(Object controller) {
    currentlyLoadedControllers.get().add(controller);
  }

  public void initalizeControllers() {
    dataStoreCallbacks.clear();
    List<DatasourceCallback> dsCallbacks = controllers.values().stream().map(l -> l.stream().map(Pair::getLeft).filter(o -> o instanceof DatasourceCallback).map(o -> (DatasourceCallback) o).collect(Collectors.toList())).reduce(new LinkedList<>(), (l, o) -> {
      l.addAll(o);
      return l;
    });
    dataStoreCallbacks.addAll(dsCallbacks);
    activityCallbacks.clear();
    List<ActivityCallback> acCallbacks = controllers.values().stream().map(l -> l.stream().map(Pair::getLeft).filter(o -> o instanceof ActivityCallback).map(o -> (ActivityCallback) o).collect(Collectors.toList())).reduce(new LinkedList<>(), (l, o) -> {
      l.addAll(o);
      return l;
    });
    activityCallbacks.addAll(acCallbacks);
    Collections.sort(dataStoreCallbacks);
  }

  public Node getViewForController(Class<?> targetController) {
    if (!controllers.containsKey(targetController)) {
      throw new IllegalArgumentException("Controller " + targetController.getName() + " is not registered. Registered are " + controllers.keySet());
    }
    LinkedList<Pair<Object, Node>> ctrls = controllers.get(targetController);
    if (ctrls.isEmpty()) {
      return null;
    } else if (ctrls.size() == 1) {
      return ctrls.get(0).getRight();
    } else {
      throw new IllegalArgumentException("There are " + ctrls.size() + " instances registered for the given controller");
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getControllerInstance(Class<T> targetController) {
    if (!controllers.containsKey(targetController)) {
      throw new IllegalArgumentException("Controller " + targetController + " is not registered. Registered are " + controllers.keySet());
    }
    LinkedList<Pair<Object, Node>> ctrls = controllers.get(targetController);
    if (ctrls.isEmpty()) {
      return null;
    } else if (ctrls.size() == 1) {
      return (T) ctrls.get(0).getLeft();
    } else {
      throw new IllegalArgumentException("There are " + ctrls.size() + " instances registered for the given controller");
    }
  }

  public <T> List<T> getControllerInstances(Class<T> targetController) {
    if (!controllers.containsKey(targetController)) {
      throw new IllegalArgumentException("Controller " + targetController + " is not registered. Registered are " + controllers.keySet());
    }
    return controllers.get(targetController).stream().map(Pair::getLeft).map(c -> (T) c).collect(Collectors.toList());
  }

  public Collection<Object> getControllers() {
    return controllers.values().stream().map(l -> l.stream().map(Pair::getLeft).collect(Collectors.toList())).reduce(new LinkedList<>(), (l, o) -> {
      l.addAll(o);
      return l;
    });
  }

  public List<DatasourceCallback> getDataStoreCallbacks() {
    return dataStoreCallbacks;
  }

  public List<ActivityCallback> getActivityCallbacks() {
    return activityCallbacks;
  }
}
