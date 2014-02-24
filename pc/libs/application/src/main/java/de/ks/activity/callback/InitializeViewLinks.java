package de.ks.activity.callback;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.activity.ActivityController;
import de.ks.activity.link.ViewLink;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 */
public class InitializeViewLinks extends LoaderCallback {
  private static final Logger log = LoggerFactory.getLogger(InitializeViewLinks.class);
  private final List<ViewLink> viewLinks;
  private final ActivityController activityController;

  public InitializeViewLinks(List<ViewLink> viewLinks, ActivityController activityController) {
    this.viewLinks = viewLinks;
    this.activityController = activityController;
  }

  @Override
  public void accept(Object controller, Node node) {
    log.debug("initializing view-links for controller {}", controller);
    for (ViewLink viewLink : viewLinks) {
      if (viewLink.getSourceController().equals(controller.getClass())) {
        EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent actionEvent) {
            activityController.getCurrentActivity().select(viewLink);
          }
        };
        addHandlerToNode(node, viewLink.getId(), handler);
        log.debug("done with view-link {} for controller {}", viewLink.getId(), controller);
      }
    }

  }
}
