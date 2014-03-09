package de.ks.menu.sink;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.NodeProvider;
import de.ks.application.Navigator;
import de.ks.eventsystem.bus.EventBus;
import de.ks.menu.MenuItemDescriptor;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 *
 */
public class ContentSink extends AbstractSink<ContentSink> {
  private static final Logger log = LoggerFactory.getLogger(ContentSink.class);

  protected Pane pane;

  @Inject
  public ContentSink(EventBus bus) {
    super(bus);
  }

  @Override
  protected void showMenuItem(Object menuItem, MenuItemDescriptor item) {
    Navigator navigator = Navigator.getNavigator(pane);
    if (navigator == null) {
      log.error("No navigator registered, can't show item {}", item);
      return;
    }
    //  pane.getChildren().clear();
    if (menuItem instanceof Node) {
      navigator.presentInMain((Node) menuItem);
      //pane.getChildren().add((Node) menuItem);
    } else if (menuItem instanceof NodeProvider) {
      NodeProvider nodeProvider = (NodeProvider) menuItem;
      navigator.presentInMain(nodeProvider.getNode());
      //pane.getChildren().add(nodeProvider.getNode());
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
