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
