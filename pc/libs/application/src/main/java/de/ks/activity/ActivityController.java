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


import com.google.common.util.concurrent.ListenableFuture;
import de.ks.activity.callback.*;
import de.ks.activity.context.ActivityContext;
import de.ks.activity.context.ActivityStore;
import de.ks.activity.link.ViewLink;
import de.ks.application.Navigator;
import de.ks.application.fxml.DefaultLoader;
import de.ks.datasource.DataSource;
import de.ks.executor.ExecutorService;
import javafx.scene.Node;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Function;

/**
 * used to control different activities and their interaction
 */
@Singleton
public class ActivityController {
  @Inject
  protected ActivityContext context;
  @Inject
  protected ActivityStore store;
  @Inject
  protected ExecutorService executorService;
  @Inject
  protected Instance<Navigator> navigator;

  protected final Deque<Activity> activities = new LinkedList<>();
  private ListenableFuture<?> dataSourceFuture;
  private DataSourceLoadingTask<Object> loadingTask;

  public void resumePreviousActivity() {
    resumePreviousActivity(null);
  }

  public void resumePreviousActivity(Object hint) {

  }

  public <T extends Activity> T start(Class<T> activityClass) {
    return start(activityClass, null, null);
  }

  public <T extends Activity> T start(Class<T> activityClass, Function toConverter, Function returnConverter) {
    T activity = CDI.current().select(activityClass).get();
    start(activity, toConverter, returnConverter);
    return activity;
  }

  public void start(Activity activity) {
    start(activity, null, null);
  }

  @SuppressWarnings("unchecked")
  public void start(Activity activity, Function toConverter, Function returnConverter) {
    Object dataSourceHint = null;
    if (context.hasCurrentActivity() && toConverter != null) {
      dataSourceHint = toConverter.apply(store.getModel());
    }
    activity.setReturnConverter(returnConverter);
    context.startActivity(activity.getClass().getName());
    DataSource dataSource = CDI.current().select(activity.getDataSource()).get();
    dataSource.setLoadingHint(dataSourceHint);
    store.setDatasource(dataSource);
    DefaultLoader<Node, Object> loader = new DefaultLoader<>(activity.getInitialController());
    addCallbacks(loader, activity);
    activity.getPreloads().put(activity.getInitialController(), loader);
    select(activity, activity.getInitialController(), Navigator.MAIN_AREA);
    loadNextControllers(activity);

    loadingTask = new DataSourceLoadingTask<>(dataSource);
    dataSourceFuture = executorService.submit(loadingTask);
    activities.add(activity);
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
      DefaultLoader<Node, Object> loader = new DefaultLoader<>(controller);
      activity.getPreloads().put(controller, loader);
      addCallbacks(loader, activity);
    }
  }


  public void waitForDataSourceLoading() {
    try {
      dataSourceFuture.get();
      loadingTask.await();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void stopCurrentResumeLast() {
    stop(getCurrentActivity().getClass().getName());
  }

  public Activity getCurrentActivity() {
    return activities.getLast();
  }

  public void stop(Activity activity) {
    String id = activity.getClass().getName();
    stop(id);
  }

  public void stop(String id) {
    context.stopActivity(id);
  }


}
