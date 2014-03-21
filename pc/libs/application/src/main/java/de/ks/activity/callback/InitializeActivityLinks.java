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

import de.ks.activity.ActivityController;
import de.ks.activity.link.ActivityLink;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;

import java.util.List;

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
