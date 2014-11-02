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

import de.ks.LauncherRunner;
import de.ks.application.Navigator;
import de.ks.eventsystem.bus.EventBus;
import de.ks.executor.JavaFXExecutorService;
import de.ks.launch.ApplicationService;
import de.ks.launch.Launcher;
import de.ks.menu.MenuItemDescriptor;
import de.ks.menu.event.MenuItemClickedEvent;
import de.ks.menu.mainmenu.About;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 */
@RunWith(LauncherRunner.class)
public class ContentSinkTest {
  private static final Logger log = LoggerFactory.getLogger(ContentSinkTest.class);
  private ContentSink sink;
  @Inject
  EventBus bus;
  JavaFXExecutorService service;

  @Before
  public void setUp() throws Exception {
    sink = CDI.current().select(ContentSink.class).get();
    sink.setMenuPath(About.MENUPATH);
    service = new JavaFXExecutorService();
  }

  @After
  public void tearDown() throws Exception {
    bus.unregister(sink);
    service.shutdown();
  }

  @Test
  public void testOpenContentInPane() throws Exception {
    VBox pane = new VBox();
    service.invokeInJavaFXThread(() -> {
      Scene scene = new Scene(pane);
      Launcher.instance.getService(ApplicationService.class).getStage().setScene(scene);
      return null;
    });
    Navigator.register(Launcher.instance.getService(ApplicationService.class).getStage(), pane);
    service.invokeInJavaFXThread(() -> {
      assertNotNull(Navigator.getNavigator(pane));
      return null;
    });
    service.invokeInJavaFXThread(() -> pane.getScene().getWindow());
    assertNotNull(Navigator.getNavigator(pane));
    assertNotNull(pane.getScene().getWindow());
    log.info("registered sink");
    sink.setPane(pane);

    bus.postAndWait(//
      new MenuItemClickedEvent(//
        new MenuItemDescriptor(About.MENUPATH, 1, About.class)));

    assertNotNull(pane.getChildren());

    assertEquals(1, pane.getChildrenUnmodifiable().size());
    Pane childPane = (Pane) pane.getChildren().get(0);
    assertEquals(1, childPane.getChildren().size());
    Label label = (Label) childPane.getChildrenUnmodifiable().get(0);
    assertEquals("hello world", label.getText());
  }
}
