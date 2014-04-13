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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Activity {
  private static final Logger log = LoggerFactory.getLogger(Activity.class);
  private final Class<? extends DataSource<?>> dataSource;
  private final Class<?> initialController;
  protected final List<ViewLink> viewLinks = new ArrayList<>();
  protected final List<TaskLink> taskLinks = new ArrayList<>();
  protected final List<ActivityLink> activityLinks = new ArrayList<>();

  protected final Map<Class<?>, DefaultLoader<Node, Object>> preloads = new HashMap<>();

  public Activity(Class<? extends DataSource<?>> dataSource, Class<?> initialController) {
    this.dataSource = dataSource;
    this.initialController = initialController;
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


  public List<ViewLink> getViewLinks() {
    return viewLinks;
  }

  public List<TaskLink> getTaskLinks() {
    return taskLinks;
  }

  public List<ActivityLink> getActivityLinks() {
    return activityLinks;
  }

  public Class<? extends DataSource<?>> getDataSource() {
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

  public Map<Class<?>, DefaultLoader<Node, Object>> getPreloads() {
    return preloads;
  }
}
