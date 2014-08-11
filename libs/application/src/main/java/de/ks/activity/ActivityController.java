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
import de.ks.activity.executor.ActivityExecutorProducer;
import de.ks.activity.executor.ActivityJavaFXExecutor;
import de.ks.activity.executor.ActivityJavaFXExecutorProducer;
import de.ks.activity.initialization.ActivityCallback;
import de.ks.activity.initialization.ActivityInitialization;
import de.ks.activity.loading.ActivityLoadingExecutor;
import de.ks.application.Navigator;
import de.ks.datasource.DataSource;
import de.ks.eventsystem.bus.EventBus;
import de.ks.executor.JavaFXExecutorService;
import de.ks.util.LockSupport;
import javafx.application.Platform;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

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
  protected Instance<Navigator> navigator;
  @Inject
  protected ActivityInitialization initialization;
  @Inject
  protected EventBus eventBus;

  @Inject
  protected ActivityExecutor executor;
  @Inject
  protected ActivityJavaFXExecutor javaFXExecutor;

  protected final AtomicReference<String> currentActivity = new AtomicReference<>();

  protected final Map<String, ActivityCfg> registeredActivities = new HashMap<>();
  protected final ReentrantLock lock = new ReentrantLock(true);
  private volatile CompletableFuture<?> finishingFutures;

  public void stopCurrentStartNew(ActivityHint activityHint) {
    loadInExecutor("could not start activityhint " + activityHint, () -> {
      stopCurrent();
      startOrResume(activityHint);
    });
  }

  public void startOrResume(ActivityHint activityHint) {
    loadInExecutor("could not start activityhint " + activityHint, () -> {
      try (LockSupport lockSupport = new LockSupport(lock)) {
        log.debug("Begin with start/resume of {} ", activityHint.getDescription());

        Object dataSourceHint = null;
        if (hasCurrentActivity() && context.hasCurrentActivity()) {
          if (activityHint.getDataSourceHint() != null) {
            dataSourceHint = activityHint.getDataSourceHint().get();
          }
          suspendCurrent();
        }

        String id = activityHint.getNextActivityId();
        if (registeredActivities.containsKey(id)) {
          resume(id, activityHint.needsReload(), dataSourceHint);
        } else {
          context.startActivity(id);
          ActivityCfg activityCfg = CDI.current().select(activityHint.getNextActivity()).get();

          registeredActivities.put(id, activityCfg);
          finishingFutures = null;

          activityCfg.setActivityHint(activityHint);

          log.info("Starting activity {}", id);
          DataSource dataSource = CDI.current().select(activityCfg.getDataSource()).get();
          dataSource.setLoadingHint(dataSourceHint);
          store.setDatasource(dataSource);

          initialization.loadActivity(activityCfg);
          initialization.getControllers().forEach(eventBus::register);

          select(activityCfg, activityCfg.getInitialController(), Navigator.MAIN_AREA);

          initialization.getActivityCallbacks().forEach(ActivityCallback::onStart);

          if (activityHint.needsReload()) {
            reload();
          }
          currentActivity.set(id);
          log.info("Started activity {}", id);
        }
      } catch (Exception e) {
        log.error("Failed to start {} because of ", activityHint.getDescription(), e);
        context.stopActivity(activityHint.getNextActivityId());
        throw e;
      } finally {
        log.debug("Done with start/resume of {} ", activityHint.getDescription());
      }
    });
  }

  protected void resume(String id, boolean reload, Object returnHint) {
    context.startActivity(id);
    log.info("Resuming activity {}", id);

    store.getDatasource().setLoadingHint(returnHint);

    ActivityCfg activityCfg = registeredActivities.get(id);

    initialization.getControllers().forEach(eventBus::register);

    select(activityCfg, activityCfg.getInitialController(), Navigator.MAIN_AREA);

    initialization.getActivityCallbacks().forEach(ActivityCallback::onResume);
    if (reload) {
      reload();
      finishingFutures.join();
    }
    currentActivity.set(id);
    log.info("Resumed activity {}", id);
  }

  protected void suspendCurrent() {
    log.debug("Suspending activity {}", getCurrentActivityId());
    initialization.getActivityCallbacks().forEach(ActivityCallback::onSuspend);

    shutdownExecutors();
    //I want to cleanup the executors themselves, but during registering, I sadly don't know that it is a producer
    //the cdi api doesn't provide that information :(
    context.cleanupSingleBean(ActivityExecutorProducer.class);
    context.cleanupSingleBean(ActivityJavaFXExecutorProducer.class);

    initialization.getControllers().forEach((controller) -> eventBus.unregister(controller));
  }

  private void shutdownExecutors() {
    executor.shutdownNow();
    javaFXExecutor.shutdownNow();
    try {
      executor.awaitTermination(5, TimeUnit.SECONDS);
      javaFXExecutor.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      //
    }
  }

  public void stopCurrent() {
    if (getCurrentActivity().getActivityHint().getReturnToActivity() != null) {
      stop(getCurrentActivityId(), false);
    } else {
      reload();
    }
  }

  public void stop(String id, boolean wait) {
    if (!registeredActivities.containsKey(id)) {
      return;
    }
    Future<?> future = loadInExecutor("could not stop activity " + id, () -> {
      try (LockSupport lockSupport = new LockSupport(lock)) {
        log.debug("Stopping activity {}", id);
        Object returnHint = null;
        String returnToActivity = null;

        if (getCurrentActivity() != null) {
          ActivityHint activityHint = getCurrentActivity().getActivityHint();
          if (activityHint.getReturnToDatasourceHint() != null) {
            returnHint = activityHint.getReturnToDatasourceHint().get();
          }
          returnToActivity = activityHint.getReturnToActivity();
        }

        context.startActivity(id);
        shutdownExecutors();
        initialization.getActivityCallbacks().forEach(ActivityCallback::onStop);
        initialization.getControllers().forEach((controller) -> eventBus.unregister(controller));
        registeredActivities.remove(id);

        context.stopActivity(id);
        if (id.equals(currentActivity.get())) {
          currentActivity.set(null);
        }
        log.debug("Stopped activity {}", id);

        if (returnToActivity != null) {
          resume(returnToActivity, true, returnHint);
        }
      } catch (Exception e) {
        log.error("Could not stop activity {}", id, e);
      }
    });
    if (wait) {
      try {
        future.get();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void stopAll() {
    HashSet<String> ids = new HashSet<>(registeredActivities.keySet());
    ids.forEach(id -> stop(id, true));
  }

  protected Future<?> loadInExecutor(String errorMsg, Runnable runnable) {
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
    return submit;
  }

  public void select(ActivityCfg activityCfg, Class<?> targetController, String presentationArea) {
    Node view = initialization.getViewForController(targetController);
    navigator.get().present(presentationArea, view);
    activityCfg.setCurrentController(targetController);
  }

  public void waitForTasks() {
    if (context.hasCurrentActivity()) {
      waitForDataSource();
      executor.waitForAllTasksDone();
      javaFXExecutor.waitForAllTasksDone();
    }
  }

  public void waitForDataSource() {
    if (finishingFutures == null || finishingFutures.isDone()) {
      return;
    }
    try (LockSupport support = new LockSupport(lock)) {
      try {
        long start = System.currentTimeMillis();
        boolean loop = true;
        while (loop) {
          try {
            finishingFutures.get(100, TimeUnit.MILLISECONDS);
            loop = false;
          } catch (TimeoutException e) {
            if (executor.isShutdown()) {
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
    }
  }

  public ActivityCfg getCurrentActivity() {
    String id = getCurrentActivityId();
    if (id == null) {
      return null;
    } else {
      return registeredActivities.get(id);
    }
  }

  public boolean hasCurrentActivity() {
    return getCurrentActivity() != null;
  }

  public String getCurrentActivityId() {
    return currentActivity.get();
  }

  public ActivityExecutor getExecutorService() {
    return executor;
  }

  public ActivityJavaFXExecutor getJavaFXExecutor() {
    return javaFXExecutor;
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

    Object model = store.getModel();
    CompletableFuture<Object> save = CompletableFuture.supplyAsync(() -> {
      log.debug("Start saving model");
      dataSource.saveModel(model, m -> {
        store.getBinding().applyControllerContent(m);
        initialization.getDataStoreCallbacks().forEach(c -> c.duringSave(m));
      });
      log.debug("Initially saved model '{}'", model);
      return model;
    }, executor);
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
    loadInExecutor("reload", () -> {
      waitForDataSource();
      DataSource dataSource = store.getDatasource();
      JavaFXExecutorService javafxExecutor = getJavaFXExecutor();

      CompletableFuture<Object> load = CompletableFuture.supplyAsync(() -> dataSource.loadModel(m -> {
        initialization.getDataStoreCallbacks().forEach(c -> c.duringLoad(m));
      }), executor);
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
