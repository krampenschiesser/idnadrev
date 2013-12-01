package de.ks.menu;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import java.util.Collection;
import java.util.function.Consumer;

/**
 *
 */
public class MenuFilter implements Consumer<Class<?>> {
  private final Collection<Class<?>> items;
  private final String menuPath;

  public MenuFilter(String menuPath, Collection<Class<?>> items) {
    this.menuPath = menuPath;
    this.items = items;
  }

  @Override
  public void accept(Class<?> o) {
    if (o == null) {
      return;
    }
    if (!o.isAnnotationPresent(MenuItem.class)) {
      return;
    }
    MenuItem annotation = o.getAnnotation(MenuItem.class);
    if (annotation.value().startsWith(menuPath)) {
      items.add(o);
    }
  }
}
