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
import de.ks.util.LockSupport;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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

  protected final Map<String, ActivityCfg> registeredActivities = new HashMap<>();
  protected final ReentrantLock lock = new ReentrantLock(true);

  public void stopCurrentStartNew(ActivityHint activityHint) {
    loadInExecutor("could not start activityhint " + activityHint, () -> {
      stopCurrent();
      startOrResume(activityHint);
    });
  }

  public void startOrResume(ActivityHint activityHint) {
    String previousActivity = context.getCurrentActivity();
    loadInExecutor("could not start activityhint " + activityHint, () -> {
      try (LockSupport lockSupport = new LockSupport(lock)) {
        log.debug("Begin with start/resume of {} ", activityHint.getDescription());
        if (isCurrentActivity(activityHint)) {
          log.debug("skip starting activity {} because it is already active");
          reload();
          store.waitForDataSource();
          return;
        }
        showBusy();

        Object dataSourceHint = null;
        if (hasCurrentActivity() && context.hasCurrentActivity()) {
          if (activityHint.getDataSourceHint() != null) {
            dataSourceHint = activityHint.getDataSourceHint().get();
          }
          suspendCurrent();
        }

        String id = activityHint.getNextActivityId();
        if (registeredActivities.containsKey(id)) {
          resume(id, activityHint.isRefreshOnReturn(), dataSourceHint, activityHint);
        } else {
          context.startActivity(id);
          ActivityCfg activityCfg = CDI.current().select(activityHint.getNextActivity()).get();

          registeredActivities.put(id, activityCfg);

          activityCfg.setActivityHint(activityHint);

          log.info("Starting activity {}", id);
          DataSource dataSource = CDI.current().select(activityCfg.getDataSource()).get();
          dataSource.setLoadingHint(dataSourceHint);
          store.setDatasource(dataSource);

          initialization.loadActivity(activityCfg);
          initialization.getControllers().forEach(eventBus::register);

          select(activityCfg, activityCfg.getInitialController(), Navigator.MAIN_AREA);

          initialization.getActivityCallbacks().forEach(ActivityCallback::onStart);

          if (activityHint.isRefreshOnReturn()) {
            reload();
            store.waitForDataSource();
          }
          log.info("Started activity {}", id);
        }
      } catch (Exception e) {
        log.error("Failed to start {} because of ", activityHint.getDescription(), e);
        context.stopActivity(activityHint.getNextActivityId());
        if (previousActivity != null) {
          context.startActivity(previousActivity);
        }
        throw e;
      } finally {
        log.debug("Done with start/resume of {} ", activityHint.getDescription());
      }
    });
  }

  protected boolean isCurrentActivity(ActivityHint activityHint) {
    String currentActivityId = getCurrentActivityId();
    if (hasCurrentActivity() && context.hasCurrentActivity()) {
      return currentActivityId.equals(activityHint.getNextActivityId());
    } else {
      return false;
    }
  }

  protected void resume(String id, boolean reload, Object returnHint, ActivityHint activityHint) {
    context.startActivity(id);
    log.info("Resuming activity {}", id);

    DataSource<?> datasource = store.getDatasource();
    datasource.setLoadingHint(returnHint);

    ActivityCfg activityCfg = registeredActivities.get(id);
    if (activityHint != null) {
      activityCfg.setActivityHint(activityHint);
    }

    initialization.getControllers().forEach(eventBus::register);

    select(activityCfg, activityCfg.getInitialController(), Navigator.MAIN_AREA);

    initialization.getActivityCallbacks().forEach(ActivityCallback::onResume);
    if (reload) {
      reload();
      store.waitForDataSource();
    }
    log.info("Resumed activity {}", id);
  }

  protected void suspendCurrent() {
    log.debug("Suspending activity {}", getCurrentActivityId());
    initialization.getActivityCallbacks().forEach(ActivityCallback::onSuspend);
    store.waitForDataSource();

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
        showBusy();
        log.debug("Stopping activity {}", id);
        Object returnHint = null;
        String returnToActivity = null;

        ActivityCfg activityCfg = registeredActivities.get(id);
        if (activityCfg != null) {
          ActivityHint activityHint = activityCfg.getActivityHint();
          if (activityHint.getReturnToDatasourceHint() != null) {
            returnHint = activityHint.getReturnToDatasourceHint().get();
          }
          returnToActivity = activityHint.getReturnToActivity();
        }

        context.startActivity(id);
        store.stop();
        initialization.getActivityCallbacks().forEach(ActivityCallback::onStop);
        initialization.getControllers().forEach((controller) -> eventBus.unregister(controller));
        store.waitForDataSource();
        shutdownExecutors();
        registeredActivities.remove(id);

        context.stopActivity(id);
        log.debug("Stopped activity {}", id);

        if (returnToActivity != null && registeredActivities.containsKey(returnToActivity)) {
          resume(returnToActivity, true, returnHint, null);
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

  protected void showBusy() {
    StackPane container = new StackPane();

    ProgressBar progress = new ProgressBar(-1);
    progress.setPrefSize(300, 25);
    progress.setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
    container.getChildren().add(progress);

    navigator.get().presentInMain(container);
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
    while (!loadingExecutor.isShutdown() && loadingExecutor.getActiveCount() > 0) {
      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException e) {
        log.trace("Got interrupted while waiting for tasks.", e);
      }
    }
    if (context.hasCurrentActivity()) {
      waitForDataSource();
      executor.waitForAllTasksDone();
      javaFXExecutor.waitForAllTasksDone();
    }
  }

  public void waitForDataSource() {
    store.waitForDataSource();
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
    return context.getCurrentActivity();
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
    store.save();
  }

  @SuppressWarnings("unchecked")
  public void reload() {
    store.reload();
  }

  @PreDestroy
  private void shutdown() {
    stopAll();
    loadingExecutor.shutdown();
    try {
      loadingExecutor.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      //i dont care
    }
  }

}
