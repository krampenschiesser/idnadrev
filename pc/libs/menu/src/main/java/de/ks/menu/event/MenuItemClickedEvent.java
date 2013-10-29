package de.ks.menu.event;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

/**
 *
 */
public class MenuItemClickedEvent {
  protected final Class<?> target;


  public MenuItemClickedEvent(Class<?> target) {
    this.target = target;
  }

  public Class<?> getTarget() {
    return target;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MenuItemClickedEvent)) return false;

    MenuItemClickedEvent that = (MenuItemClickedEvent) o;

    if (target != null ? !target.equals(that.target) : that.target != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return target != null ? target.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "MenuItemClickedEvent{" +
            "target=" + target.getSimpleName() +
            '}';
  }
}
