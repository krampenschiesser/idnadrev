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
public class MenuFilter implements Consumer<Object> {
  private final Collection<Object> items;
  private final String menuPath;

  public MenuFilter(String menuPath, Collection<Object> items) {
    this.menuPath = menuPath;
    this.items = items;
  }

  @Override
  public void accept(Object o) {
    if (o == null) {
      return;
    }
    if (!o.getClass().isAnnotationPresent(MenuItem.class)) {
      return;
    }
    MenuItem annotation = o.getClass().getAnnotation(MenuItem.class);
    if (annotation.value().startsWith(menuPath)) {
      items.add(o);
    }
  }
}
