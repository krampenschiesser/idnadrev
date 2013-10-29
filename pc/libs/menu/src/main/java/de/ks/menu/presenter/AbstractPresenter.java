package de.ks.menu.presenter;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.eventsystem.EventSystem;
import de.ks.menu.MenuExtension;
import de.ks.menu.event.MenuItemClickedEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javax.inject.Inject;

/**
 *
 */
public abstract class AbstractPresenter<T> {
  @Inject
  MenuExtension menuExtension;

  public abstract T getMenu(String menuPath);

  public EventHandler<ActionEvent> createDefaultActionHandler(Class<?> target) {
    return new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        EventSystem.bus.post(new MenuItemClickedEvent(target));
      }
    };
  }
}
