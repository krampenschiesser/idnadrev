package de.ks.activity.link;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

/**
 *
 */
public class ViewLink {
  public static class ViewLinkBuilder {
    private final Class<?> sourceController;
    private String id;
    private Class<?> targetController;
    private String presentationArea;

    public ViewLinkBuilder(Class<?> sourceController) {
      this.sourceController = sourceController;
    }

    public ViewLinkBuilder with(String id) {
      this.id = id;
      return this;
    }

    public ViewLinkBuilder to(Class<?> targetController) {
      this.targetController = targetController;
      return this;
    }

    public ViewLinkBuilder in(String presentationArea) {
      this.presentationArea = presentationArea;
      return this;
    }

    public ViewLink build() {
      return new ViewLink(sourceController, id, targetController, presentationArea);
    }
  }

  public static ViewLinkBuilder from(Class<?> sourceController) {
    return new ViewLinkBuilder(sourceController);
  }

  private final Class<?> sourceController;
  private final String id;
  private final Class<?> targetController;
  private final String presentationArea;

  protected ViewLink(Class<?> sourceController, String id, Class<?> targetController, String presentationArea) {
    this.sourceController = sourceController;
    this.id = id;
    this.targetController = targetController;
    this.presentationArea = presentationArea;
  }

  public Class<?> getSourceController() {
    return sourceController;
  }

  public String getId() {
    return id;
  }

  public Class<?> getTargetController() {
    return targetController;
  }

  public String getPresentationArea() {
    return presentationArea;
  }
}
