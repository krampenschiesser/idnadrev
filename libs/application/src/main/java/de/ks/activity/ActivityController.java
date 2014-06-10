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

package de.ks.activity;

import de.ks.activity.context.ActivityContext;
import de.ks.activity.context.ActivityStore;
import de.ks.activity.executor.ActivityExecutor;
import de.ks.activity.initialization.ActivityInitialization;
import de.ks.activity.link.ViewLink;
import de.ks.application.Navigator;
import de.ks.datasource.DataSource;
import de.ks.eventsystem.bus.EventBus;
import de.ks.executor.JavaFXExecutorService;
import de.ks.executor.SuspendablePooledExecutorService;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * used to control different activities and their interaction
 */
@Singleton
public class ActivityController {
  private static final Logger log = LoggerFactory.getLogger(ActivityController.class);
  @Inject
  protected ActivityContext context;
  @Inject
  protected ActivityStore store;
  @Inject
  protected ActivityExecutor executor;
  @Inject
  protected Instance<Navigator> navigator;
  @Inject
  protected JavaFXExecutorService javafxExecutor;
  @Inject
  protected ActivityInitialization initialization;

  protected final Deque<ActivityCfg> activities = new LinkedList<>();
  protected final Map<String, ActivityCfg> registeredActivities = new HashMap<>();
  protected final ReentrantLock lock = new ReentrantLock(true);
  private CompletableFuture<?> finishingFutures;

  public void resumePreviousActivity() {
    resumePreviousActivity(null);
  }

  public void resumePreviousActivity(Object hint) {
    lock.lock();
    try {
      Iterator<ActivityCfg> activityIterator = activities.descendingIterator();
      ActivityCfg current = activityIterator.next();
      if (activityIterator.hasNext()) {
        ActivityCfg previous = activityIterator.next();
        log.info("Resuming previous activity {}, current={}", previous.getClass().getName(), current.getClass().getName());
        stop(current);
        resume(previous, hint);
      } else {
        log.info("Relaoding current activity {}", current.getClass().getName());
        reload();
      }
    } finally {
      lock.unlock();
    }
  }

  private void resume(ActivityCfg activityCfg, Object dataSourceHint) {
    String id = activityCfg.getClass().getName();
    context.startActivity(id);
    executor.startOrResume(id);
    log.info("Resuming activity {}", id);
    DataSource dataSource = CDI.current().select(activityCfg.getDataSource()).get();
    dataSource.setLoadingHint(dataSourceHint);
    store.setDatasource(dataSource);
    select(activityCfg, activityCfg.getInitialController(), Navigator.MAIN_AREA);
    reload();
    log.info("Resumed activity {} with hint", id, dataSourceHint);
  }

  public <T extends ActivityCfg> T start(Class<T> activityClass) {
    return start(activityClass, null, null);
  }

  public <T extends ActivityCfg> T start(Class<T> activityClass, Function toConverter, Function returnConverter) {
    T activity = CDI.current().select(activityClass).get();
    start(activity, toConverter, returnConverter);
    return activity;
  }

  public void start(ActivityCfg activityCfg) {
    start(activityCfg, null, null);
  }

  @SuppressWarnings("unchecked")
  public void start(ActivityCfg activityCfg, Function toConverter, Function returnConverter) {
    lock.lock();
    try {
      Object dataSourceHint = null;
      if (context.hasCurrentActivity() && toConverter != null) {
        dataSourceHint = toConverter.apply(store.getModel());
      }
      activityCfg.setReturnConverter(returnConverter);
      String id = activityCfg.getClass().getName();
      context.startActivity(id);
      executor.startOrResume(id);
      activities.add(activityCfg);
      registeredActivities.put(id, activityCfg);

      log.info("Starting activity {}", id);
      DataSource dataSource = CDI.current().select(activityCfg.getDataSource()).get();
      dataSource.setLoadingHint(dataSourceHint);
      store.setDatasource(dataSource);

      initialization.loadActivity(activityCfg);
      select(activityCfg, activityCfg.getInitialController(), Navigator.MAIN_AREA);

      reload();
      log.info("Started activity {}", id);
    } finally {
      lock.unlock();
    }

  }

  public void select(ActivityCfg activityCfg, ViewLink link) {
    select(activityCfg, link.getTargetController(), link.getPresentationArea());
  }

  public void select(ActivityCfg activityCfg, Class<?> targetController, String presentationArea) {
    Node view = initialization.getViewForController(targetController);
    navigator.get().present(presentationArea, view);
    activityCfg.setCurrentController(targetController);
  }

  public void waitForDataSource() {
    lock.lock();
    try {
      finishingFutures.join();
    } finally {
      lock.unlock();
    }
  }

  public ActivityCfg getCurrentActivity() {
    return activities.getLast();
  }

  public String getCurrentActivityId() {
    return activities.getLast().getId();
  }

  public void stop(Class<? extends ActivityCfg> activityClass) {
    lock.lock();
    try {
      String activityId = activityClass.getName();
      ActivityCfg activityCfg = registeredActivities.get(activityId);
      if (activityCfg == null) {
        log.warn("Could not stop unregistered activity {}", activityId);
        return;
      }
      waitForDataSource();
      String id = activityId;
      executor.shutdown(id);
      stop(id);
    } finally {
      lock.unlock();
    }
  }

  public void stop(ActivityCfg activityCfg) {
    stop(activityCfg.getClass());

  }

  protected void stop(String id) {
    context.stopActivity(id);
  }

  public SuspendablePooledExecutorService getCurrentExecutorService() {
    return executor.getActivityExecutorService(getCurrentActivityId());
  }

  public JavaFXExecutorService getJavaFXExecutor() {
    return javafxExecutor;
  }

  @SuppressWarnings("unchecked")
  public <T extends Node> T getCurrentNode() {
    Class<?> currentController = getCurrentActivity().getCurrentController();
    return getNodeForController(currentController);
  }

  @SuppressWarnings("unchecked")
  public <T extends Node> T getNodeForController(Class<?> controller) {
    return (T) initialization.getViewForController(controller);
  }

  @SuppressWarnings("unchecked")
  public <T> T getCurrentController() {
    return (T) getControllerInstance(getCurrentActivity().getCurrentController());
  }

  @SuppressWarnings("unchecked")
  public <T> T getControllerInstance(Class<T> controller) {
    return (T) initialization.getControllerInstance(controller);
  }

  @SuppressWarnings("unchecked")
  public void save() {
    waitForDataSource();
    DataSource dataSource = store.getDatasource();
    SuspendablePooledExecutorService executorService = executor.getActivityExecutorService(getCurrentActivityId());

    Object model = store.getModel();
    CompletableFuture<Object> save = CompletableFuture.supplyAsync(() -> {
      store.getBinding().applyControllerContent(model);
      dataSource.saveModel(model);
      return model;
    }, executorService);
    finishingFutures = save.thenApplyAsync((value) -> {
      log.debug("Saved model '{}'", value);
      return value;
    });

    save.exceptionally((t) -> {
      log.error("Could not save model {} DataSource {} for activity {}", model, dataSource, getCurrentActivityId(), t);
      return null;
    });
  }

  @SuppressWarnings("unchecked")
  public void reload() {
    DataSource dataSource = store.getDatasource();
    SuspendablePooledExecutorService executorService = executor.getActivityExecutorService(getCurrentActivityId());

    CompletableFuture<Object> load = CompletableFuture.supplyAsync(() -> dataSource.loadModel(), executorService);
    finishingFutures = load.thenApplyAsync((value) -> {
      log.debug("Loaded model '{}'", value);
      CDI.current().select(ActivityStore.class).get().setModel(value);
      return value;
    }, javafxExecutor).thenAcceptAsync((value) -> {
      EventBus eventBus = CDI.current().select(EventBus.class).get();
      eventBus.post(new ActivityLoadFinishedEvent(value));
    }, javafxExecutor);

    load.exceptionally((t) -> {
      log.error("Could not load DataSource {} for activity {}", dataSource, getCurrentActivityId(), t);
      return null;
    });
  }
}
