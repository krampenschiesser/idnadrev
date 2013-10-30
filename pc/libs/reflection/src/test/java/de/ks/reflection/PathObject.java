package de.ks.reflection;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

/**
 *
 */
public class PathObject {
  private PathContext context;
  private String name;

  protected PathObject() {
  }

  public PathObject(String name) {
    this.name = name;
  }

  public void setContext(PathContext context) {
    this.context = context;
  }

  public PathContext getContext() {
    return context;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
