package de.ks.menu.sink;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.eventsystem.bus.EventBus;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

/**
 *
 */
public class ContentSink extends AbstractSink<ContentSink> {
  private static final Logger log = LogManager.getLogger(ContentSink.class);

  protected Pane pane;

  @Inject
  public ContentSink(EventBus bus) {
    super(bus);
  }

  @Override
  protected void showMenuItem(Object menuItem) {
    pane.getChildren().clear();
    if (menuItem instanceof Node) {
      pane.getChildren().add((Node) menuItem);
    } else {
      log.error("Could not handle MenuItemClickedEvent because {} is not a {}", menuItem.getClass(), Node.class.getName());
    }
  }

  public Pane getPane() {
    return pane;
  }

  public ContentSink setPane(Pane pane) {
    this.pane = pane;
    return this;
  }
}
