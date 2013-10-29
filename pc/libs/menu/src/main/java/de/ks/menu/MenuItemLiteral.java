package de.ks.menu;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.enterprise.util.AnnotationLiteral;

/**
 *
 */
public class MenuItemLiteral extends AnnotationLiteral<MenuItem> implements MenuItem {
  @Override
  public String value() {
    return "";
  }

  @Override
  public String image() {
    return "";
  }
}
