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

import de.ks.activity.ActivityCfg;
import de.ks.activity.ActivityController;
import de.ks.activity.callback.*;
import de.ks.activity.context.ActivityScoped;
import de.ks.activity.link.ViewLink;
import de.ks.application.fxml.DefaultLoader;
import de.ks.executor.JavaFXExecutorService;
import de.ks.executor.SuspendablePooledExecutorService;
import javafx.fxml.LoadException;
import javafx.scene.Node;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@ActivityScoped
public class ActivityInitialization {
  private static final Logger log = LoggerFactory.getLogger(ActivityInitialization.class);

  protected final ConcurrentHashMap<Class<?>, Pair<Object, Node>> controllers = new ConcurrentHashMap<>();
  protected final List<LoaderCallback> callbacks = new ArrayList<>();
  protected final Map<Class<?>, CompletableFuture<DefaultLoader<Node, Object>>> preloads = new HashMap<>();
  protected final ThreadLocal<List<Object>> currentlyLoadedControllers = ThreadLocal.withInitial(ArrayList::new);

  @Inject
  ActivityController controller;

  public void loadActivity(ActivityCfg activityCfg) {
    loadControllers(activityCfg);
    setupDefaultCallbacks(activityCfg);
    initalizeControllers();
  }

  private void setupDefaultCallbacks(ActivityCfg activityCfg) {
    callbacks.add(new InitializeViewLinks(activityCfg));
    callbacks.add(new InitializeActivityLinks(activityCfg));
    callbacks.add(new InitializeTaskLinks(activityCfg));
    callbacks.add(new InitializeModelBindings(activityCfg));
    callbacks.add(new InitializeListBindings(activityCfg));
  }

  protected void loadControllers(ActivityCfg activityCfg) {
    loadController(activityCfg.getInitialController());
    for (ViewLink next : activityCfg.getViewLinks()) {
      loadController(next.getSourceController());
      loadController(next.getTargetController());
    }
  }

  private void loadController(Class<?> controllerClass) {
    SuspendablePooledExecutorService executorService = controller.getCurrentExecutorService();
    JavaFXExecutorService javaFXExecutor = controller.getJavaFXExecutor();

    if (!preloads.containsKey(controllerClass)) {
      DefaultLoader<Node, Object> loader = new DefaultLoader<>(controllerClass);
      Supplier<DefaultLoader<Node, Object>> supplier = () -> {

        loader.load();
        Node view = loader.getView();

        currentlyLoadedControllers.get().forEach((c) -> {
          assert c != null;
          assert view != null;
          log.debug("Registering controller {} with node {}", c, view);
          controllers.put(c.getClass(), Pair.of(c, view));
        });
        currentlyLoadedControllers.get().clear();
        return loader;
      };

      CompletableFuture<DefaultLoader<Node, Object>> loaderFuture = CompletableFuture.supplyAsync(supplier, executorService).exceptionally((t) -> {
        if (t.getCause() instanceof RuntimeException && t.getCause().getCause() instanceof LoadException) {
          currentlyLoadedControllers.get().clear();
          log.info("Last load of {} failed, will try again in JavaFX Thread", loader.getFxmlFile());
          return javaFXExecutor.invokeInJavaFXThread(() -> supplier.get());
        }
        throw new RuntimeException(t);
      });

      preloads.put(controllerClass, loaderFuture);
    }
  }

  public ActivityInitialization addCallback(LoaderCallback callback) {
    callbacks.add(callback);
    return this;
  }

  public void addControllerToInitialize(Object controller) {
    currentlyLoadedControllers.get().add(controller);
  }

  public void initalizeControllers() {
    scanControllers();
    doChangesInFXThread();
  }

  protected void scanControllers() {
    SuspendablePooledExecutorService executorService = controller.getCurrentExecutorService();

    Profiler time1 = new Profiler("time1");
    time1.setLogger(log);
    Optional<CompletableFuture<Void>> future = callbacks.stream().map((c) -> CompletableFuture.runAsync(() -> //
            controllers.values().forEach((pair) -> c.accept(pair.getLeft(), pair.getRight())), executorService))//
            .reduce((first, second) -> CompletableFuture.allOf(first, second));
    future.get().join();
    log.debug("Done with initialization of {} controllers for activity {}. Took {}ns", controllers.size(), controller.getCurrentActivityId(), time1.stop().elapsedTime());
  }

  protected void doChangesInFXThread() {
    JavaFXExecutorService javaFXExecutor = controller.getJavaFXExecutor();

    Profiler time1 = new Profiler("time1");
    time1.setLogger(log);
    Optional<CompletableFuture<Void>> future = callbacks.stream().map((c) -> CompletableFuture.runAsync(() -> //
            controllers.values().forEach((pair) -> c.doInFXThread(pair.getLeft(), pair.getRight())), javaFXExecutor))//
            .reduce((first, second) -> CompletableFuture.allOf(first, second));
    future.get().join();
    log.debug("Done with submitting changes to FX Thread of {} controllers for activity {}. Took {}ns", controllers.size(), controller.getCurrentActivityId(), time1.stop().elapsedTime());
  }

  public Node getViewForController(Class<?> targetController) {
    if (!controllers.containsKey(targetController)) {
      throw new IllegalArgumentException("Controller " + targetController.getName() + " is not registered. Registered are " + controllers.keySet());
    }
    return controllers.get(targetController).getRight();
  }

  public Object getControllerInstance(Class<?> targetController) {
    if (!controllers.containsKey(targetController)) {
      throw new IllegalArgumentException("Controller " + targetController.getClass() + " is not registered. Registered are " + controllers.keySet());
    }
    return controllers.get(targetController).getLeft();
  }
}
