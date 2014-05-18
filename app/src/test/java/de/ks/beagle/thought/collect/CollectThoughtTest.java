/**
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
package de.ks.beagle.thought.collect;

import de.ks.application.Launcher;
import de.ks.application.Navigator;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertNotNull;

//@RunWith(ApplicationRunner.class)
public class CollectThoughtTest {
  private static final Logger log = LoggerFactory.getLogger(CollectThoughtTest.class);
  private Scene scene;

  @Before
  public void setUp() throws Exception {
    Launcher.instance.start(new String[0]);
    Launcher.instance.waitForInitialization();
    CountDownLatch latch1 = new CountDownLatch(1);
    Platform.runLater(() -> latch1.countDown());
    latch1.await();
    Navigator currentNavigator = Navigator.getCurrentNavigator();
    assertNotNull(currentNavigator);
    Pane content = currentNavigator.getMainArea().getContent();
    scene = content.getScene();

    CountDownLatch latch2 = new CountDownLatch(1);
    Platform.runLater(() -> {
      scene.getWindow().hide();
      latch2.countDown();
    });
    latch2.await();
  }

  @Test
  public void testSaveThought() throws Exception {
    Parent root = scene.getRoot();
    Stack<Node> nodes = new Stack<>();
    root.getChildrenUnmodifiable().forEach(node -> nodes.add(node));
    while (!nodes.isEmpty()) {
      Node node = nodes.pop();
      if (node instanceof Parent) {
        ObservableList<Node> children = ((Parent) node).getChildrenUnmodifiable();
        nodes.addAll(children);
      }
      log.info("{}  -> {}", node.getClass().getSimpleName(), node);
    }
  }
}
