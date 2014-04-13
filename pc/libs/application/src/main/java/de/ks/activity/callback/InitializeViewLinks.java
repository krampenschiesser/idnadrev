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

package de.ks.activity.callback;


import de.ks.activity.Activity;
import de.ks.activity.ActivityController;
import de.ks.activity.link.ViewLink;
import de.ks.application.fxml.LoaderCallback;
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
  private final Activity activity;
  private final List<ViewLink> viewLinks;
  private final ActivityController activityController;

  public InitializeViewLinks(Activity activity, List<ViewLink> viewLinks, ActivityController activityController) {
    this.activity = activity;
    this.viewLinks = viewLinks;
    this.activityController = activityController;
  }

  @Override
  public void accept(Object controller, Node node) {
    log.debug("initializing view-links for controller {}", controller);
    viewLinks.stream().filter(viewLink -> viewLink.getSourceController().equals(controller.getClass())).forEach(viewLink -> {
      EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
          activityController.select(activity, viewLink);
        }
      };
      addHandlerToNode(node, viewLink.getId(), handler);
      log.debug("done with view-link {} for controller {}", viewLink.getId(), controller);
    });

  }
}
