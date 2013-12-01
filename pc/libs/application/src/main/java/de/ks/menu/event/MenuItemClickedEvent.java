package de.ks.menu.event;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.menu.MenuItemDescriptor;

/**
 *
 */
public class MenuItemClickedEvent {
  protected final MenuItemDescriptor item;

  public MenuItemClickedEvent(MenuItemDescriptor item) {
    this.item = item;
  }

  public Class<?> getTarget() {
    return item.getTarget();
  }

  public MenuItemDescriptor getItem() {
    return item;
  }
}
