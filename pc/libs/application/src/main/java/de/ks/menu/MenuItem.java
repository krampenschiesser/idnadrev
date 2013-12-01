package de.ks.menu;/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.enterprise.util.Nonbinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MenuItem {
  public static final String SEPERATOR = "/";

  /**
   * The name of the menu
   */
  @Nonbinding String value();


  /**
   * An optional path to an item image.
   */
  @Nonbinding String image() default "";
}
