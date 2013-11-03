package de.ks.menu.sink;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.JFXCDIRunner;
import de.ks.eventsystem.EventSystem;
import de.ks.menu.MenuItemDescriptor;
import de.ks.menu.event.MenuItemClickedEvent;
import de.ks.menu.mainmenu.About;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
public class ContentSinkTest {
  private ContentSink sink;

  @Before
  public void setUp() throws Exception {
    sink = CDI.current().select(ContentSink.class).get();
    sink.setMenuPath(About.MENUPATH);
  }


  @Test
  public void testOpenContentInPane() throws Exception {
    VBox pane = new VBox();
    sink.setPane(pane);

    EventSystem.bus.postAndWait(//
            new MenuItemClickedEvent(//
                    new MenuItemDescriptor(About.MENUPATH, About.class)));

    assertNotNull(pane.getChildren());

    assertEquals(1, pane.getChildrenUnmodifiable().size());
    Pane childPane = (Pane) pane.getChildren().get(0);
    assertEquals(1, childPane.getChildren().size());
    Label label = (Label) childPane.getChildrenUnmodifiable().get(0);
    assertEquals("hello world", label.getText());
  }
}
