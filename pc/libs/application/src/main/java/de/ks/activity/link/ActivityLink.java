/*
 * Copyright [2014] [Christian Loehnert]
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

package de.ks.activity.link;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.activity.Activity;

/**
 *
 */
public class ActivityLink {
  public static class ActivityLinkBuilder {
    private final Class<?> sourceController;
    private String id;
    private Activity nextActivity;

    public ActivityLinkBuilder(Class<?> sourceController) {
      this.sourceController = sourceController;
    }

    public ActivityLinkBuilder with(String id) {
      this.id = id;
      return this;
    }

    public ActivityLinkBuilder start(Activity next) {
      this.nextActivity = next;
      return this;
    }

    public ActivityLink build() {
      return new ActivityLink(sourceController, id, nextActivity);
    }
  }

  public static ActivityLinkBuilder from(Class<?> sourceController) {
    return new ActivityLinkBuilder(sourceController);
  }

  protected final Class<?> sourceController;
  protected final String id;
  protected final Activity nextActivity;

  private ActivityLink(Class<?> sourceController, String id, Activity nextActivity) {
    this.sourceController = sourceController;
    this.id = id;
    this.nextActivity = nextActivity;
  }

  public Class<?> getSourceController() {
    return sourceController;
  }

  public String getId() {
    return id;
  }

  public Activity getNextActivity() {
    return nextActivity;
  }
}
