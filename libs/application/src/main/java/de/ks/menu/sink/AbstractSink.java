/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ks.menu.sink;

import com.google.common.eventbus.Subscribe;
import de.ks.activity.ActivityController;
import de.ks.eventsystem.bus.EventBus;
import de.ks.eventsystem.bus.HandlingThread;
import de.ks.eventsystem.bus.Threading;
import de.ks.menu.MenuItem;
import de.ks.menu.MenuItemDescriptor;
import de.ks.menu.event.MenuItemClickedEvent;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 *
 */
public abstract class AbstractSink<T extends AbstractSink> {
  protected final ActivityController controller;
  protected final EventBus bus;
  protected String menuPath;

  @Inject
  @MenuItem("")
  Instance<Object> menuItem;

  @Inject
  public AbstractSink(EventBus bus, ActivityController controller) {
    this.bus = bus;
    this.controller = controller;
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
  @Threading(HandlingThread.JavaFX)
  public boolean onEvent(MenuItemClickedEvent event) {
    if (getMenuPath() != null) {
      if (event.getItem().getMenuPath().startsWith(getMenuPath())) {
        Instance<?> select = menuItem.select(event.getTarget());
        Object menuItem = select.get();

        showMenuItem(menuItem, event.getItem());
        return true;
      }
    }
    return false;
  }

  protected abstract void showMenuItem(Object menuItem, MenuItemDescriptor item);
}
