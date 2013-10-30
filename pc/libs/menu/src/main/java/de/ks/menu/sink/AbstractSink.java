package de.ks.menu.sink;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.ks.menu.event.MenuItemClickedEvent;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

/**
 *
 */
public abstract class AbstractSink<T extends AbstractSink> {
  protected EventBus bus;
  protected String menuPath;

  @Inject
  public AbstractSink(EventBus bus) {
    this.bus = bus;
  }

  @SuppressWarnings("unchecked")
  public T setMenuPath(String path) {
    this.menuPath = path;
    return (T) this;
  }

  protected EventBus getBus() {
    return bus;
  }

  public String getMenuPath() {
    return menuPath;
  }

  @PostConstruct
  public void register() {
    bus.register(this);
  }

  @PreDestroy
  public void deregister() {
    bus.unregister(this);
  }

  @Subscribe
  public abstract void onEvent(MenuItemClickedEvent event);
}
