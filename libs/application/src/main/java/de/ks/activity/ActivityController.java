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

package de.ks.activity;


import de.ks.activity.callback.*;
import de.ks.activity.context.ActivityContext;
import de.ks.activity.context.ActivityStore;
import de.ks.activity.executor.ActivityExecutor;
import de.ks.activity.link.ViewLink;
import de.ks.application.Navigator;
import de.ks.application.fxml.DefaultLoader;
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
import java.util.concurrent.ExecutorService;
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

  protected final Deque<Activity> activities = new LinkedList<>();
  protected final Map<String, Activity> registeredActivities = new HashMap<>();
  protected final ReentrantLock lock = new ReentrantLock(true);
  private CompletableFuture<?> finishingFutures;

  public void resumePreviousActivity() {
    resumePreviousActivity(null);
  }

  public void resumePreviousActivity(Object hint) {
    lock.lock();
    try {
      Iterator<Activity> activityIterator = activities.descendingIterator();
      Activity current = activityIterator.next();
      if (activityIterator.hasNext()) {
        Activity previous = activityIterator.next();
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

  private void resume(Activity activity, Object dataSourceHint) {
    String id = activity.getClass().getName();
    context.startActivity(id);
    executor.startOrResume(id);
    log.info("Resuming activity {}", id);
    DataSource dataSource = CDI.current().select(activity.getDataSource()).get();
    dataSource.setLoadingHint(dataSourceHint);
    store.setDatasource(dataSource);
    select(activity, activity.getInitialController(), Navigator.MAIN_AREA);
    reload();
    log.info("Resumed activity {} with hint", id, dataSourceHint);
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

  public <T extends Activity> T start(Class<T> activityClass) {
    return start(activityClass, null, null);
  }

  public <T extends Activity> T start(Class<T> activityClass, Function toConverter, Function returnConverter) {
    T activity = CDI.current().select(activityClass).get();
    start(activity, toConverter, returnConverter, true);
    return activity;
  }

  public void start(Activity activity) {
    start(activity, null, null, true);
  }

  @SuppressWarnings("unchecked")
  public void start(Activity activity, Function toConverter, Function returnConverter, boolean loadControllers) {
    lock.lock();
    try {
      Object dataSourceHint = null;
      if (context.hasCurrentActivity() && toConverter != null) {
        dataSourceHint = toConverter.apply(store.getModel());
      }
      activity.setReturnConverter(returnConverter);
      String id = activity.getClass().getName();
      context.startActivity(id);
      executor.startOrResume(id);

      log.info("Starting activity {}", id);
      DataSource dataSource = CDI.current().select(activity.getDataSource()).get();
      dataSource.setLoadingHint(dataSourceHint);
      store.setDatasource(dataSource);

      if (loadControllers) {
        DefaultLoader<Node, Object> loader = new DefaultLoader<>(activity.getInitialController(), executor.getActivityExecutorService(id));
        addCallbacks(loader, activity);
        activity.getPreloads().put(activity.getInitialController(), loader);
      }
      select(activity, activity.getInitialController(), Navigator.MAIN_AREA);
      if (loadControllers) {
        loadNextControllers(activity);
      }

      activities.add(activity);
      registeredActivities.put(id, activity);
      reload();
      log.info("Started activity {}", id);
    } finally {
      lock.unlock();
    }
  }

  private void addCallbacks(DefaultLoader<Node, Object> loader, Activity activity) {
    loader.addCallback(new InitializeViewLinks(activity, activity.getViewLinks(), this));
    loader.addCallback(new InitializeActivityLinks(activity.getActivityLinks(), this));
    loader.addCallback(new InitializeTaskLinks(activity.getTaskLinks(), activity, this));
    loader.addCallback(new InitializeModelBindings(activity, store));
    loader.addCallback(new InitializeListBindings(activity, store));
  }


  public void select(Activity activity, ViewLink link) {
    select(activity, link.getTargetController(), link.getPresentationArea());
  }

  public void select(Activity activity, Class<?> targetController, String presentationArea) {
    DefaultLoader<Node, Object> loader = activity.getPreloads().get(targetController);
    navigator.get().present(presentationArea, loader.getView());
    activity.setCurrentController(targetController);
  }

  protected void loadNextControllers(Activity activity) {
    for (ViewLink next : activity.getViewLinks()) {
      loadController(activity, next.getSourceController());
      loadController(activity, next.getTargetController());
    }
  }

  private void loadController(Activity activity, Class<?> controller) {
    if (!activity.getPreloads().containsKey(controller)) {
      DefaultLoader<Node, Object> loader = new DefaultLoader<>(controller, executor.getActivityExecutorService(activity.getId()));
      activity.getPreloads().put(controller, loader);
      addCallbacks(loader, activity);
    }
  }


  public void waitForDataSourceLoading() {
    lock.lock();
    try {
      finishingFutures.join();
    } finally {
      lock.unlock();
    }
  }

  public Activity getCurrentActivity() {
    return activities.getLast();
  }

  public String getCurrentActivityId() {
    return activities.getLast().getId();
  }

  public void stop(Class<? extends Activity> activityClass) {
    lock.lock();
    try {
      String activityId = activityClass.getName();
      Activity activity = registeredActivities.get(activityId);
      if (activity == null) {
        log.warn("Could not stop unregistered activity {}", activityId);
        return;
      }
      activity.waitForInitialization();
      waitForDataSourceLoading();
      String id = activityId;
      executor.shutdown(id);
      stop(id);
    } finally {
      lock.unlock();
    }
  }

  public void stop(Activity activity) {
    stop(activity.getClass());

  }

  protected void stop(String id) {
    context.stopActivity(id);
  }

  public ExecutorService getCurrentExecutorService() {
    return executor.getActivityExecutorService(getCurrentActivityId());
  }
}
