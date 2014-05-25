/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
 *
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

package de.ks.menu.presenter;


import de.ks.eventsystem.bus.EventBus;
import de.ks.menu.MenuExtension;
import de.ks.menu.MenuItemDescriptor;
import de.ks.menu.event.MenuItemClickedEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 *
 */
public abstract class AbstractPresenter<T> {
  private static final Logger log = LoggerFactory.getLogger(AbstractPresenter.class);
  @Inject
  MenuExtension menuExtension;
  @Inject
  EventBus bus;

  public abstract T getMenu(String menuPath);

  @SuppressWarnings("unchecked")
  public EventHandler<ActionEvent> createDefaultActionHandler(MenuItemDescriptor item) {
    return event -> {
      log.debug("Sending event for item {}", item.getTarget().getSimpleName());
      bus.post(new MenuItemClickedEvent(item));
    };
  }
}
