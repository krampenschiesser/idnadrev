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


import javafx.concurrent.Task;

/**
 *
 */
public class TaskLink {
  public static TaskLinkBuilder from(Class<?> sourceController) {
    return new TaskLinkBuilder(sourceController);
  }

  public static class TaskLinkBuilder {
    private final Class<?> sourceController;
    private String id;
    private Class<? extends Task<?>> task;
    private boolean end = false;

    public TaskLinkBuilder(Class<?> sourceController) {
      this.sourceController = sourceController;
    }

    public TaskLinkBuilder with(String id) {
      this.id = id;
      return this;
    }

    public TaskLinkBuilder execute(Class<? extends Task<?>> task) {
      this.task = task;
      return this;
    }

    public TaskLinkBuilder endActivity() {
      this.end = true;
      return this;
    }

    public TaskLink build() {
      return new TaskLink(sourceController, id, task, end);
    }

  }

  private final Class<?> sourceController;
  private final String id;
  private final Class<? extends Task<?>> task;
  private final boolean end;

  protected TaskLink(Class<?> sourceController, String id, Class<? extends Task<?>> task, boolean end) {
    this.sourceController = sourceController;
    this.id = id;
    this.task = task;
    this.end = end;
  }

  public boolean isEnd() {
    return end;
  }

  public Class<?> getSourceController() {
    return sourceController;
  }

  public String getId() {
    return id;
  }

  public Class<? extends Task<?>> getTask() {
    return task;
  }
}
