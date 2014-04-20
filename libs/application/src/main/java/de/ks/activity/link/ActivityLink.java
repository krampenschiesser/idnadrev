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

package de.ks.activity.link;


import de.ks.activity.Activity;

import java.util.function.Function;

/**
 *
 */
public class ActivityLink {
  public static class ActivityLinkBuilder {
    private final Class<?> sourceController;
    private String id;
    private Class<? extends Activity> nextActivity;
    private Function toConverter;
    private Function returnConverter;

    public ActivityLinkBuilder(Class<?> sourceController) {
      this.sourceController = sourceController;
    }

    public ActivityLinkBuilder with(String id) {
      this.id = id;
      return this;
    }

    public ActivityLinkBuilder start(Class<? extends Activity> next) {
      this.nextActivity = next;
      return this;
    }

    public ActivityLinkBuilder toConverter(Function toConverter) {
      this.toConverter = toConverter;
      return this;
    }

    public ActivityLinkBuilder returnConverter(Function returnConverter) {
      this.returnConverter = returnConverter;
      return this;
    }

    public ActivityLink build() {
      return new ActivityLink(sourceController, id, nextActivity, toConverter, returnConverter);
    }
  }

  public static ActivityLinkBuilder from(Class<?> sourceController) {
    return new ActivityLinkBuilder(sourceController);
  }

  protected final Class<?> sourceController;
  protected final String id;
  protected final Class<? extends Activity> nextActivity;
  protected final Function toConverter;
  protected final Function returnConverter;

  private ActivityLink(Class<?> sourceController, String id, Class<? extends Activity> nextActivity, Function toConverter, Function returnConverter) {
    this.sourceController = sourceController;
    this.id = id;
    this.nextActivity = nextActivity;
    this.toConverter = toConverter;
    this.returnConverter = returnConverter;
  }

  public Class<?> getSourceController() {
    return sourceController;
  }

  public String getId() {
    return id;
  }

  public Class<? extends Activity> getNextActivity() {
    return nextActivity;
  }

  public Function getToConverter() {
    return toConverter;
  }

  public Function getReturnConverter() {
    return returnConverter;
  }
}
