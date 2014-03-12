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
import de.ks.workflow.Workflow;
import de.ks.workflow.cdi.WorkflowContext;
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

  public abstract T getMenu(String menuPath);

  @SuppressWarnings("unchecked")
  public EventHandler<ActionEvent> createDefaultActionHandler(MenuItemDescriptor item) {
    return event -> {
      if (Workflow.class.isAssignableFrom(item.getTarget())) {
        WorkflowContext.start((Class<? extends Workflow>) item.getTarget());
      }
      log.debug("Sending event for item {}", item.getTarget().getSimpleName());
      EventSystem.bus.post(new MenuItemClickedEvent(item));
    };
  }
}
