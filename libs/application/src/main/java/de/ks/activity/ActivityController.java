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
import de.ks.activity.loading.ActivityLoadingExecutor;
import de.ks.application.Navigator;
import de.ks.datasource.DataSource;
import de.ks.eventsystem.bus.EventBus;
import de.ks.executor.JavaFXExecutorService;
import de.ks.executor.SuspendablePooledExecutorService;
import javafx.application.Platform;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * used to control different activities and their interaction
 */
@Singleton
public class ActivityController {
  private static final Logger log = LoggerFactory.getLogger(ActivityController.class);
  protected final ActivityLoadingExecutor loadingExecutor = new ActivityLoadingExecutor();

  @Inject
  protected ActivityContext context;
  @Inject
  protected ActivityStore store;
  @Inject
  protected ActivityExecutor executor;
  @Inject
  protected Instance<Navigator> navigator;
  @Inject
  protected ActivityInitialization initialization;
  @Inject
  protected EventBus eventBus;

  protected final Deque<ActivityCfg> activities = new LinkedList<>();
  protected final Map<String, ActivityCfg> registeredActivities = new HashMap<>();
  protected final ReentrantLock lock = new ReentrantLock(true);
  private CompletableFuture<?> finishingFutures;

  public void resumePreviousActivity() {
    resumePreviousActivity(null);
  }

  public void resumePreviousActivity(Object hint) {
    loadInExecutor("could not resume previous activity", () -> {
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
          log.info("Reloading current activity {}", current.getClass().getName());
          reload();
        }
      } finally {
        lock.unlock();
      }
    });
  }

  private void resume(ActivityCfg activityCfg, Object dataSourceHint) {
    String id = activityCfg.getClass().getName();
    context.startActivity(id);
    executor.startOrResume(id);
    log.info("Resuming activity {}", id);
    DataSource dataSource = CDI.current().select(activityCfg.getDataSource()).get();
    dataSource.setLoadingHint(dataSourceHint);
    store.setDatasource(dataSource);

    initialization.getControllers().forEach((controller) -> eventBus.register(controller));

    select(activityCfg, activityCfg.getInitialController(), Navigator.MAIN_AREA);
    reload();
    log.info("Resumed activity {} with hint", id, dataSourceHint);
  }

  public <T extends ActivityCfg> void start(Class<T> activityClass) {
    start(activityClass, null, null);
  }

  public <T extends ActivityCfg> void start(Class<T> activityClass, Function toConverter, Function returnConverter) {
    T activity = CDI.current().select(activityClass).get();
    start(activity, toConverter, returnConverter);
  }

  public void start(ActivityCfg activityCfg) {
    start(activityCfg, null, null);
  }

  @SuppressWarnings("unchecked")
  public void start(ActivityCfg activityCfg, Function toConverter, Function returnConverter) {

    loadInExecutor("couldn ot start " + activityCfg, () -> {
      lock.lock();
      try {
        Object dataSourceHint = null;
        if (context.hasCurrentActivity() && toConverter != null) {
          dataSourceHint = toConverter.apply(store.getModel());
        }
        activityCfg.setReturnConverter(returnConverter);
        String id = activityCfg.getClass().getName();

        if (context.hasCurrentActivity()) {
          initialization.getControllers().forEach((controller) -> eventBus.unregister(controller));
          this.executor.suspend(getCurrentActivityId());
        }

        context.startActivity(id);
        executor.startOrResume(id);
        activities.add(activityCfg);
        registeredActivities.put(id, activityCfg);

        log.info("Starting activity {}", id);
        DataSource dataSource = CDI.current().select(activityCfg.getDataSource()).get();
        dataSource.setLoadingHint(dataSourceHint);
        store.setDatasource(dataSource);

        initialization.loadActivity(activityCfg);
        initialization.getControllers().forEach((controller) -> eventBus.register(controller));

        select(activityCfg, activityCfg.getInitialController(), Navigator.MAIN_AREA);
        reload();
        log.info("Started activity {}", id);
      } finally {
        lock.unlock();
      }
    });
  }

  protected void loadInExecutor(String errorMsg, Runnable runnable) {
    Future<?> submit = loadingExecutor.submit(runnable);
    if (!Platform.isFxApplicationThread()) {
      try {
        submit.get();
      } catch (InterruptedException e) {
        //
      } catch (ExecutionException e) {
        log.error(errorMsg, e);
        throw new RuntimeException(e);
      }
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

  public void waitForTasks() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      //
    }
    while (loadingExecutor.getActiveCount() > 0) {
      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException e) {
        log.trace("Got interrupted while waiting for tasks.", e);
      }
    }
    getCurrentExecutorService().waitForAllTasksDone();
    getJavaFXExecutor().waitForAllTasksDone();
  }

  public void waitForDataSource() {
    if (finishingFutures == null || finishingFutures.isDone()) {
      return;
    } else if (getCurrentExecutorService().isSuspended() || getCurrentExecutorService().isShutdown()) {
      return;
    }
    lock.lock();
    try {
      try {
        long start = System.currentTimeMillis();
        boolean loop = true;
        while (loop) {
          try {
            finishingFutures.get(100, TimeUnit.MILLISECONDS);
            loop = false;
          } catch (TimeoutException e) {
            if (getCurrentExecutorService().isSuspended() || getCurrentExecutorService().isShutdown()) {
              loop = false;
            } else {
              loop = true;
            }
            if (System.currentTimeMillis() - start > TimeUnit.SECONDS.toMillis(10)) {
              throw new IllegalStateException("Waited for 10s for datasource, did not return.");
            }
          }
        }
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    } finally {
      lock.unlock();
    }
  }

  protected void checkNotFXThread() {
    if (Platform.isFxApplicationThread()) {
      throw new IllegalThreadStateException("Operation not allowed in javafx thread");
    }
  }

  public ActivityCfg getCurrentActivity() {
    return activities.getLast();
  }

  public String getCurrentActivityId() {
    return activities.getLast().getId();
  }

  public void stopCurrentStart(Class<? extends ActivityCfg> next) {
    stop(getCurrentActivity());
    start(next);
  }

  public void stop(Class<? extends ActivityCfg> activityClass) {
    loadInExecutor("could not stop " + activityClass, () -> {
      lock.lock();
      try {
        String activityId = activityClass.getName();
        ActivityCfg activityCfg = registeredActivities.get(activityId);
        if (activityCfg == null) {
          log.warn("Could not stop unregistered activity {}", activityId);
          return;
        }
        String id = activityId;
        executor.shutdown(id);
        if (context.hasCurrentActivity()) {
          initialization.getControllers().forEach((controller) -> eventBus.unregister(controller));
        }
        activities.removeLastOccurrence(activityCfg);
        stop(id);
      } finally {
        lock.unlock();
      }
    });
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
    return executor.getJavaFXExecutorService(getCurrentActivityId());
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
      log.debug("Start saving model");
      dataSource.saveModel(model, m -> {
        store.getBinding().applyControllerContent(model);
        initialization.getDataStoreCallbacks().forEach(c -> c.duringSave(m));
      });
      log.debug("Initially saved model '{}'", model);
      return model;
    }, executorService);
    finishingFutures = save.thenApply((value) -> {
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
    waitForDataSource();
    DataSource dataSource = store.getDatasource();
    SuspendablePooledExecutorService executorService = getCurrentExecutorService();
    JavaFXExecutorService javafxExecutor = getJavaFXExecutor();

    CompletableFuture<Object> load = CompletableFuture.supplyAsync(() -> dataSource.loadModel(m -> {
      initialization.getDataStoreCallbacks().forEach(c -> c.duringLoad(m));
    }), executorService);
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

  @PreDestroy
  private void shutdown() {
    loadingExecutor.shutdown();
    try {
      loadingExecutor.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      //i dont care
    }
  }
}
