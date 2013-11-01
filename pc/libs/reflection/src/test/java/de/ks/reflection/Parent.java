package de.ks.reflection;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

/**
 *
 */
public class Parent {
  private String privateParentField;
  protected String protectedParentField;
  public String publicParentField;

  public void parentMethod() {

  }

  private void privateParentMethod() {

  }

  protected void protectedParentMethod() {

  }

  protected String overriden() {
    return "parent";
  }
}
