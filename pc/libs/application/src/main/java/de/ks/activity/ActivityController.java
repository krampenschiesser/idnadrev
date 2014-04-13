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

  @SuppressWarnings("unchecked")
  public void start(Activity activity) {
    context.startActivity(activity.toString());
    DataSource dataSource = CDI.current().select(activity.getDataSource()).get();
    store.setDatasource(dataSource);
    DefaultLoader<Node, Object> loader = new DefaultLoader<>(activity.getInitialController());
    addCallbacks(loader, activity);
    activity.getPreloads().put(activity.getInitialController(), loader);
    select(activity, activity.getInitialController(), Navigator.MAIN_AREA);
    loadNextControllers(activity);

    dataSourceFuture = executorService.submit(new DataSourceLoadingTask<>(dataSource));
    activities.add(activity);
  }

  private void addCallbacks(DefaultLoader<Node, Object> loader, Activity activity) {
    loader.addCallback(new InitializeViewLinks(activity, activity.getViewLinks(), this));
    loader.addCallback(new InitializeActivityLinks(activity.getActivityLinks(), this));
    loader.addCallback(new InitializeTaskLinks(activity.getTaskLinks(), this));
    loader.addCallback(new InitializeModelBindings(activity, store));
    loader.addCallback(new InitializeListBindings(activity, store));
  }


  public void select(Activity activity, ViewLink link) {
    select(activity, link.getTargetController(), link.getPresentationArea());
  }

  public void select(Activity activity, Class<?> targetController, String presentationArea) {
    DefaultLoader<Node, Object> loader = activity.getPreloads().get(targetController);
    navigator.get().present(presentationArea, loader.getView());
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
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void stopCurrentResumeLast() {
    context.stopActivity(getCurrentActivity().toString());
  }

  public Activity getCurrentActivity() {
    return activities.getLast();
  }

  public void stop(Activity activity) {
    context.stopActivity(activity.toString());
  }
}
