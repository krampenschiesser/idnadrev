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

package de.ks.activity.link;

import de.ks.activity.ActivityCfg;

/**
 *
 */
public class ActivityLink {
  public static class ActivityLinkBuilder {
    private final Class<?> sourceController;
    private String id;
    private Class<? extends ActivityCfg> nextActivity;
    private ActivityHint activityHint;

    public ActivityLinkBuilder(Class<?> sourceController) {
      this.sourceController = sourceController;
    }

    public ActivityLinkBuilder with(String id) {
      this.id = id;
      return this;
    }

    public ActivityLinkBuilder start(Class<? extends ActivityCfg> next) {
      this.nextActivity = next;
      return this;
    }

    public ActivityLinkBuilder navigationHint(ActivityHint hint) {
      this.activityHint = hint;
      return this;
    }

    public ActivityLink build() {
      return new ActivityLink(sourceController, id, nextActivity, activityHint);
    }
  }

  public static ActivityLinkBuilder from(Class<?> sourceController) {
    return new ActivityLinkBuilder(sourceController);
  }

  protected final Class<?> sourceController;
  protected final String id;
  protected final Class<? extends ActivityCfg> nextActivity;
  protected final ActivityHint activityHint;

  private ActivityLink(Class<?> sourceController, String id, Class<? extends ActivityCfg> nextActivity, ActivityHint hint) {
    this.sourceController = sourceController;
    this.id = id;
    this.nextActivity = nextActivity;
    this.activityHint = hint;
  }

  public Class<?> getSourceController() {
    return sourceController;
  }

  public String getId() {
    return id;
  }

  public Class<? extends ActivityCfg> getNextActivity() {
    return nextActivity;
  }

  public ActivityHint getActivityHint() {
    return activityHint;
  }
}
