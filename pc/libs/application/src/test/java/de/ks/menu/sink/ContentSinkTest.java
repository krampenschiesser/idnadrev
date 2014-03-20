/*
 * Copyright [${YEAR}] [Christian Loehnert]
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

import de.ks.JFXCDIRunner;
import de.ks.application.Navigator;
import de.ks.eventsystem.EventSystem;
import de.ks.executor.ExecutorService;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 */
@RunWith(JFXCDIRunner.class)
public class ContentSinkTest {
  private static final Logger log = LoggerFactory.getLogger(ContentSinkTest.class);
  private ContentSink sink;

  @Before
  public void setUp() throws Exception {
    sink = CDI.current().select(ContentSink.class).get();
    sink.setMenuPath(About.MENUPATH);
  }

  @After
  public void tearDown() throws Exception {
    EventSystem.bus.unregister(sink);
  }

  @Test
  public void testOpenContentInPane() throws Exception {
    VBox pane = new VBox();
    ExecutorService.instance.invokeInJavaFXThread(() -> {
      Scene scene = new Scene(pane);
      JFXCDIRunner.getStage().setScene(scene);
    });
    Navigator.register(JFXCDIRunner.getStage(), pane);
    ExecutorService.instance.invokeInJavaFXThread(() -> assertNotNull(Navigator.getNavigator(pane)));
    ExecutorService.instance.invokeInJavaFXThread(() -> pane.getScene().getWindow());
    assertNotNull(Navigator.getNavigator(pane));
    assertNotNull(pane.getScene().getWindow());
    log.info("registered sink");
    sink.setPane(pane);

    EventSystem.bus.postAndWait(//
            new MenuItemClickedEvent(//
                    new MenuItemDescriptor(About.MENUPATH, About.class))
    );

    assertNotNull(pane.getChildren());

    assertEquals(1, pane.getChildrenUnmodifiable().size());
    Pane childPane = (Pane) pane.getChildren().get(0);
    assertEquals(1, childPane.getChildren().size());
    Label label = (Label) childPane.getChildrenUnmodifiable().get(0);
    assertEquals("hello world", label.getText());
  }
}
