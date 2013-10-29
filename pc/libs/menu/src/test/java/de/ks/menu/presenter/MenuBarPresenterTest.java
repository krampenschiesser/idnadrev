package de.ks.menu.presenter;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.JFXCDIRunner;
import de.ks.i18n.Localized;
import de.ks.imagecache.Images;
import de.ks.menu.mainmenu.About;
import de.ks.menu.mainmenu.Keymap;
import de.ks.menu.mainmenu.Open;
import de.ks.menu.mainmenu.Save;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.CDI;

import static org.junit.Assert.*;

/**
 *
 */
@RunWith(JFXCDIRunner.class)
public class MenuBarPresenterTest {
  private static Thread thread;
  //  private CdiContainer cdiContainer;
  private MenuBarPresenter presenter;

//  @BeforeClass
//  public static void beforeClass() {
//    JFXTestApp.startApp();
//  }

  @Before
  public void setUp() throws Exception {
//    cdiContainer = CdiContainerLoader.getCdiContainer();
//    cdiContainer.boot(null);
//    cdiContainer.boot();

    presenter = CDI.current().select(MenuBarPresenter.class).get();
  }

//  @After
//  public void tearDown() throws Exception {
//    cdiContainer.shutdown();
//  }

  @Test
  public void testGetMenu() throws Exception {
    MenuBar menu = presenter.getMenu("/main");
    assertEquals(2, menu.getMenus().size());

    Menu fileMenu = menu.getMenus().sorted().get(0);
    assertEquals(Localized.get(Open.MENUPATH), fileMenu.getText());

    Menu optionsMenu = menu.getMenus().sorted().get(1);
    assertEquals(Localized.get(About.MENUPATH), optionsMenu.getText());

    assertEquals(2, fileMenu.getItems().size());
    assertEquals(Localized.get(Open.ITEMPATH), fileMenu.getItems().get(0).getText());
    assertEquals(Localized.get(Save.ITEMPATH), fileMenu.getItems().get(1).getText());

    assertEquals(2, optionsMenu.getItems().size());
    assertEquals(Localized.get(About.ITEMPATH), optionsMenu.getItems().get(0).getText());
    assertEquals(Localized.get(Keymap.MENUPATH), optionsMenu.getItems().get(1).getText());

    Menu preferencesMenu = (Menu) optionsMenu.getItems().get(1);
    assertEquals(1, preferencesMenu.getItems().size());
    assertEquals(Localized.get(Keymap.ITEMPATH), preferencesMenu.getItems().get(0).getText());

    MenuItem keymapItem = preferencesMenu.getItems().get(0);
    assertNotNull(keymapItem.getGraphic());
    assertEquals(ImageView.class, keymapItem.getGraphic().getClass());
    ImageView view = (ImageView) keymapItem.getGraphic();
    assertSame(Images.get("keymap.jpg"), view.getImage());
  }
}
