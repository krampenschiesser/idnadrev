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


import de.ks.activity.callback.InitializeActivityLinks;
import de.ks.activity.callback.InitializeModelBindings;
import de.ks.activity.callback.InitializeTaskLinks;
import de.ks.activity.callback.InitializeViewLinks;
import de.ks.activity.context.ActivityStore;
import de.ks.activity.link.ActivityLink;
import de.ks.activity.link.TaskLink;
import de.ks.activity.link.ViewLink;
import de.ks.application.Navigator;
import de.ks.application.fxml.DefaultLoader;
import de.ks.datasource.DataSource;
import javafx.concurrent.Task;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Activity {
  private static final Logger log = LoggerFactory.getLogger(Activity.class);
  private final DataSource<?> dataSource;
  private final Class<?> initialController;
  private final ActivityController activityController;
  private final Navigator navigator;
  protected final List<ViewLink> viewLinks = new ArrayList<>();
  protected final List<TaskLink> taskLinks = new ArrayList<>();
  protected final List<ActivityLink> activityLinks = new ArrayList<>();

  protected final Map<Class<?>, DefaultLoader<Node, Object>> preloads = new HashMap<>();

  public Activity(DataSource<?> dataSource, Class<?> initialController, ActivityController activityController, Navigator navigator) {
    this.dataSource = dataSource;
    this.initialController = initialController;
    this.activityController = activityController;
    this.navigator = navigator;
  }

  /**
   * Select next controller
   *
   * @param sourceController
   * @param id
   * @param targetController
   * @return
   */
  public Activity withLink(Class<?> sourceController, String id, Class<?> targetController) {
    return withLink(sourceController, id, Navigator.MAIN_AREA, targetController);
  }

  public Activity withLink(Class<?> sourceController, String id, String presentationArea, Class<?> targetController) {
    ViewLink viewLink = ViewLink.from(sourceController).with(id).to(targetController).in(presentationArea).build();
    viewLinks.add(viewLink);
    return this;
  }

  /**
   * Execute given task
   *
   * @param sourceController
   * @param id
   * @param task
   * @return
   */
  public Activity withTask(Class<?> sourceController, String id, Class<? extends Task<?>> task) {
    TaskLink taskLink = TaskLink.from(sourceController).with(id).execute(task).build();
    taskLinks.add(taskLink);
    return this;
  }

  /**
   * Switch to next activity
   *
   * @param sourceController
   * @param id
   * @param next
   * @return
   */
  public Activity withActivity(Class<?> sourceController, String id, Activity next) {
    ActivityLink activityLink = ActivityLink.from(sourceController).with(id).start(next).build();
    activityLinks.add(activityLink);
    return this;
  }

  public Class<?> getInitialController() {
    return initialController;
  }

  public void start() {
    DefaultLoader<Node, Object> loader = new DefaultLoader<>(initialController);
    addCallbacks(loader);
    preloads.put(initialController, loader);
    select(initialController, Navigator.MAIN_AREA);
    loadNextControllers();
  }

  protected void loadNextControllers() {
    for (ViewLink next : viewLinks) {
      loadController(next.getSourceController());
      loadController(next.getTargetController());
    }
  }

  private void loadController(Class<?> controller) {
    if (!preloads.containsKey(controller)) {
      DefaultLoader<Node, Object> loader = new DefaultLoader<>(controller);
      preloads.put(controller, loader);
      addCallbacks(loader);
    }
  }

  public void select(ViewLink link) {
    select(link.getTargetController(), link.getPresentationArea());
  }

  public void select(Class<?> targetController, String presentationArea) {
    DefaultLoader<Node, Object> loader = preloads.get(targetController);
    navigator.present(presentationArea, loader.getView());
  }

  private void addCallbacks(DefaultLoader<Node, Object> loader) {
    loader.addCallback(new InitializeViewLinks(viewLinks, activityController));
    loader.addCallback(new InitializeActivityLinks(activityLinks, activityController));
    loader.addCallback(new InitializeTaskLinks(taskLinks, activityController));
    loader.addCallback(new InitializeModelBindings(this, CDI.current().select(ActivityStore.class).get()));
  }

  public List<ViewLink> getViewLinks() {
    return viewLinks;
  }

  public List<TaskLink> getTaskLinks() {
    return taskLinks;
  }

  public List<ActivityLink> getActivityLinks() {
    return activityLinks;
  }

  public DataSource<?> getDataSource() {
    return dataSource;
  }

  public boolean isInitialized() {
    for (DefaultLoader<Node, Object> loader : preloads.values()) {
      if (!loader.isLoaded()) {
        return false;
      }
    }
    return true;
  }

  public void waitForInitialization() {
    for (DefaultLoader<Node, Object> loader : preloads.values()) {
      loader.waitForLoading();
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getController(Class<T> controllerClass) {
    return (T) preloads.get(controllerClass).getController();
  }

  @SuppressWarnings("unchecked")
  public <V> V getView(Class<?> controllerClass) {
    return (V) preloads.get(controllerClass).getView();
  }
}
