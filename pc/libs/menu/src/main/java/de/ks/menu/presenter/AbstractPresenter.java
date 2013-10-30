package de.ks.menu.presenter;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.eventsystem.EventSystem;
import de.ks.menu.MenuExtension;
import de.ks.menu.MenuItemDescriptor;
import de.ks.menu.event.MenuItemClickedEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

/**
 *
 */
public abstract class AbstractPresenter<T> {
  private static final Logger log = LogManager.getLogger(AbstractPresenter.class);
  @Inject
  MenuExtension menuExtension;

  public abstract T getMenu(String menuPath);

  public EventHandler<ActionEvent> createDefaultActionHandler(MenuItemDescriptor item) {
    return new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        log.debug("Sending event for item {}", item.getTarget().getSimpleName());
        EventSystem.bus.post(new MenuItemClickedEvent(item));
      }
    };
  }
}
