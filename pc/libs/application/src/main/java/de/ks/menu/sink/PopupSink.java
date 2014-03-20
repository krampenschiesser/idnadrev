/*
 * Copyright [2014] [Christian Loehnert]
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

package de.ks.menu.sink;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.NodeProvider;
import de.ks.activity.ActivityController;
import de.ks.eventsystem.bus.EventBus;
import de.ks.i18n.Localized;
import de.ks.menu.MenuItemDescriptor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 *
 */
public class PopupSink extends AbstractSink<PopupSink> {
  private static final Logger log = LoggerFactory.getLogger(PopupSink.class);
  private Stage mockStage = null;

  @Inject
  public PopupSink(EventBus bus, ActivityController controller) {
    super(bus, controller);
  }

  protected void showMenuItem(Object menuItem, MenuItemDescriptor item) {
    Stage stage = createStage(item.getMenuItemPath());
    if (menuItem instanceof Parent) {
      stage.setScene(new Scene((Parent) menuItem));
    } else if (menuItem instanceof Node) {
      StackPane stackPane = new StackPane();
      stackPane.getChildren().add((Node) menuItem);
      stage.setScene(new Scene(stackPane));
    } else if (menuItem instanceof NodeProvider) {
      NodeProvider nodeProvider = (NodeProvider) menuItem;

      StackPane stackPane = new StackPane();
      stackPane.getChildren().add(nodeProvider.getNode());

      stage.setScene(new Scene(stackPane));
    } else {
      log.error("Could not handle MenuItemClickedEvent because {} is neither {} nor {}", menuItem.getClass(), Parent.class.getName(), Node.class.getName());
    }
    stage.show();
  }

  private Stage createStage(String title) {
    Stage retval;
    if (mockStage != null) {
      retval = mockStage;
    } else {
      retval = new Stage(StageStyle.UTILITY);
    }
    retval.setTitle(Localized.get(title));
    return retval;
  }

  protected void mockWindow(Stage stage) {
    mockStage = stage;
  }
}
