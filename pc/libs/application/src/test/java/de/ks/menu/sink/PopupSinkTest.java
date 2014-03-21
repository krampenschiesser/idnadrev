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


import de.ks.JFXCDIRunner;
import de.ks.eventsystem.EventSystem;
import de.ks.i18n.Localized;
import de.ks.menu.MenuItemDescriptor;
import de.ks.menu.event.MenuItemClickedEvent;
import de.ks.menu.mainmenu.About;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.CDI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 */
@RunWith(JFXCDIRunner.class)
public class PopupSinkTest {
  private PopupSink sink;
  private Stage stage;

  @Before
  public void setUp() throws Exception {
    sink = CDI.current().select(PopupSink.class).get();
    sink.setMenuPath(About.MENUPATH);
    stage = JFXCDIRunner.JFXTestApp.stage;
    sink.mockWindow(stage);
  }

  @After
  public void tearDown() throws Exception {
    EventSystem.bus.unregister(sink);
  }

  @Test
  public void testNullMenuPath() throws Exception {
    sink.setMenuPath(null);

    EventSystem.bus.postAndWait(//
            new MenuItemClickedEvent(//
                    new MenuItemDescriptor(About.MENUPATH, About.class))
    );
  }

  @Test
  public void testOpenPopupForParent() throws Exception {
    EventSystem.bus.postAndWait(//
            new MenuItemClickedEvent(//
                    new MenuItemDescriptor(About.MENUPATH, About.class))
    );
    assertNotNull(stage.getScene());
    Parent root = stage.getScene().getRoot();
    assertNotNull(root);

    assertEquals(1, root.getChildrenUnmodifiable().size());
    Label label = (Label) root.getChildrenUnmodifiable().get(0);
    assertEquals("hello world", label.getText());

    assertEquals(Localized.get(About.ITEMPATH), stage.getTitle());
  }
}
