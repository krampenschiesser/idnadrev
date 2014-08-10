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

import de.ks.activity.link.ActivityHint;
import de.ks.activity.link.ActivityLink;
import de.ks.activity.link.TaskLink;
import de.ks.activity.link.ViewLink;
import de.ks.application.Navigator;
import de.ks.datasource.DataSource;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ActivityCfg {
  private static final Logger log = LoggerFactory.getLogger(ActivityCfg.class);
  protected final Class<? extends DataSource<?>> dataSource;
  protected final Class<?> initialController;
  protected final List<ViewLink> viewLinks = new ArrayList<>();
  protected final List<TaskLink> taskLinks = new ArrayList<>();
  protected final List<ActivityLink> activityLinks = new ArrayList<>();
  protected final List<Class<?>> additionalControllers = new ArrayList<>();

  private Class<?> currentController;
  private ActivityHint activityHint;

  public ActivityCfg(Class<? extends DataSource<?>> dataSource, Class<?> initialController) {
    this.dataSource = dataSource;
    this.initialController = initialController;
  }

  public ActivityCfg withLink(Class<?> sourceController, String id, Class<?> targetController) {
    return withLink(sourceController, id, Navigator.MAIN_AREA, targetController);
  }

  public ActivityCfg withLink(Class<?> sourceController, String id, String presentationArea, Class<?> targetController) {
    ViewLink viewLink = ViewLink.from(sourceController).with(id).to(targetController).in(presentationArea).build();
    viewLinks.add(viewLink);
    return this;
  }

  public ActivityCfg withTask(String id, Class<? extends Task<?>> task) {
    return withTask(getInitialController(), id, task);
  }

  /**
   * Execute given task
   *
   * @param sourceController
   * @param id
   * @param task
   * @return
   */
  public ActivityCfg withTask(Class<?> sourceController, String id, Class<? extends Task<?>> task) {
    TaskLink taskLink = TaskLink.from(sourceController).with(id).execute(task).build();
    taskLinks.add(taskLink);
    return this;
  }

  public ActivityCfg withEnd(String id, Class<? extends Task<?>> task) {
    return withEnd(getInitialController(), id, task);
  }

  public ActivityCfg withEnd(Class<?> sourceController, String id, Class<? extends Task<?>> task) {
    TaskLink taskLink = TaskLink.from(sourceController).with(id).execute(task).endActivity().build();
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
  public ActivityCfg withActivity(Class<?> sourceController, String id, Class<? extends ActivityCfg> next) {
    ActivityLink activityLink = ActivityLink.from(sourceController).with(id).start(next).build();
    activityLinks.add(activityLink);
    return this;
  }

  public <T, R> ActivityCfg withActivity(Class<?> sourceController, String id, Class<? extends ActivityCfg> next, ActivityHint hint) {
    ActivityLink activityLink = ActivityLink.from(sourceController).with(id).start(next).navigationHint(hint).build();
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

  public List<Class<?>> getAdditionalControllers() {
    return additionalControllers;
  }

  public void addAdditionalController(Class<?> controller) {
    additionalControllers.add(controller);
  }

  public Class<? extends DataSource<?>> getDataSource() {
    return dataSource;
  }

  public String getId() {
    return getClass().getName();
  }

  public void setCurrentController(Class<?> currentController) {
    this.currentController = currentController;
  }

  public Class<?> getCurrentController() {
    return currentController;
  }

  public void setActivityHint(ActivityHint activityHint) {
    this.activityHint = activityHint;
  }

  public ActivityHint getActivityHint() {
    return activityHint;
  }
}
