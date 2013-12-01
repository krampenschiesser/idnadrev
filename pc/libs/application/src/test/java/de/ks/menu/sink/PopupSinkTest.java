package de.ks.menu.sink;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.JFXCDIRunner;
import de.ks.eventsystem.EventSystem;
import de.ks.i18n.Localized;
import de.ks.menu.MenuItemDescriptor;
import de.ks.menu.event.MenuItemClickedEvent;
import de.ks.menu.mainmenu.About;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.Stage;
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

  @Test
  public void testNullMenuPath() throws Exception {
    sink.setMenuPath(null);

    EventSystem.bus.post(//
            new MenuItemClickedEvent(//
                    new MenuItemDescriptor(About.MENUPATH, About.class)));
  }

  @Test
  public void testOpenPopupForParent() throws Exception {
    EventSystem.bus.postAndWait(//
            new MenuItemClickedEvent(//
                    new MenuItemDescriptor(About.MENUPATH, About.class)));
    assertNotNull(stage.getScene());
    Parent root = stage.getScene().getRoot();
    assertNotNull(root);

    assertEquals(1, root.getChildrenUnmodifiable().size());
    Label label = (Label) root.getChildrenUnmodifiable().get(0);
    assertEquals("hello world", label.getText());

    assertEquals(Localized.get(About.ITEMPATH), stage.getTitle());
  }
}
