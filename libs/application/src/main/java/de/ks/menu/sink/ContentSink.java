/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
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

package de.ks.menu.sink;

import de.ks.NodeProvider;
import de.ks.activity.ActivityCfg;
import de.ks.activity.ActivityController;
import de.ks.application.Navigator;
import de.ks.eventsystem.bus.EventBus;
import de.ks.executor.ExecutorService;
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
  protected ExecutorService executorService;

  @Inject
  public ContentSink(EventBus bus, ActivityController controller) {
    super(bus, controller);
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
    } else if (menuItem instanceof ActivityCfg) {
      executorService.submit(() -> controller.start((ActivityCfg) menuItem));
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
