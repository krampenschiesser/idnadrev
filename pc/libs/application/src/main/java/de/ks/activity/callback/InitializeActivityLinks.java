package de.ks.activity.callback;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.activity.ActivityController;
import de.ks.activity.link.ActivityLink;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;

import java.util.List;

/**
 *
 */
public class InitializeActivityLinks extends LoaderCallback {
  private final List<ActivityLink> activityLinks;
  private final ActivityController activityController;

  public InitializeActivityLinks(List<ActivityLink> activityLinks, ActivityController activityController) {
    this.activityLinks = activityLinks;
    this.activityController = activityController;
  }

  @Override
  public void accept(Object controller, Node node) {
    for (ActivityLink activityLink : activityLinks) {
      if (activityLink.getSourceController().equals(controller.getClass())) {
        EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent actionEvent) {
            activityController.start(activityLink.getNextActivity());
          }
        };
        addHandlerToNode(node, activityLink.getId(), handler);
      }
    }

  }
}
