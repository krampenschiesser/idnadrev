package de.ks.reflection;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

/**
 *
 */
public class PathContext {
  private String name;

  public PathContext setName(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }
}
