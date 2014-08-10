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

import de.ks.datasource.DataSource;
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
  protected final List<Class<?>> additionalControllers = new ArrayList<>();

  private Class<?> currentController;
  private ActivityHint activityHint;

  public ActivityCfg(Class<? extends DataSource<?>> dataSource, Class<?> initialController) {
    this.dataSource = dataSource;
    this.initialController = initialController;
  }

  public Class<?> getInitialController() {
    return initialController;
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
