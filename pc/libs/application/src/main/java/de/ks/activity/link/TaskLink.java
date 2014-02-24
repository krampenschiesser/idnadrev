package de.ks.activity.link;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

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

    public TaskLink build() {
      return new TaskLink(sourceController, id, task);
    }
  }

  private final Class<?> sourceController;
  private final String id;
  private final Class<? extends Task<?>> task;

  protected TaskLink(Class<?> sourceController, String id, Class<? extends Task<?>> task) {
    this.sourceController = sourceController;
    this.id = id;
    this.task = task;
  }
}
