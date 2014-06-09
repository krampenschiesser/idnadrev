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

package de.ks.menu.presenter;

import com.google.common.eventbus.Subscribe;
import de.ks.LauncherRunner;
import de.ks.eventsystem.bus.EventBus;
import de.ks.i18n.Localized;
import de.ks.imagecache.Images;
import de.ks.menu.MenuItemDescriptor;
import de.ks.menu.event.MenuItemClickedEvent;
import de.ks.menu.mainmenu.About;
import de.ks.menu.mainmenu.Keymap;
import de.ks.menu.mainmenu.Open;
import de.ks.menu.mainmenu.Save;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import static org.junit.Assert.*;

/**
 *
 */
@RunWith(LauncherRunner.class)
public class MenuBarPresenterTest {
  private MenuBarPresenter presenter;
  private MenuItemDescriptor eventItem;
  @Inject
  EventBus bus;

  @Before
  public void setUp() throws Exception {
    presenter = CDI.current().select(MenuBarPresenter.class).get();
  }

  @Test
  public void testGetMenu() throws Exception {
    MenuBar menu = presenter.getMenu("/main");
    assertEquals(2, menu.getMenus().size());

    Menu fileMenu = menu.getMenus().sorted().get(0);
    assertEquals(Localized.get(getTranslationKey(Open.MENUPATH)), fileMenu.getText());
    assertFalse(fileMenu.getText().contains("/"));

    Menu optionsMenu = menu.getMenus().sorted().get(1);
    assertEquals(Localized.get(getTranslationKey(About.MENUPATH)), optionsMenu.getText());

    assertEquals(2, fileMenu.getItems().size());
    assertEquals(Localized.get(getTranslationKey(Open.ITEMPATH)), fileMenu.getItems().get(0).getText());
    assertFalse(fileMenu.getItems().get(0).getText().contains("/"));
    assertEquals(Localized.get(getTranslationKey(Save.ITEMPATH)), fileMenu.getItems().get(1).getText());

    assertEquals(2, optionsMenu.getItems().size());
    assertEquals(Localized.get(getTranslationKey(About.ITEMPATH)), optionsMenu.getItems().get(0).getText());
    assertEquals(Localized.get(getTranslationKey(Keymap.MENUPATH)), optionsMenu.getItems().get(1).getText());

    Menu preferencesMenu = (Menu) optionsMenu.getItems().get(1);
    assertEquals(1, preferencesMenu.getItems().size());
    assertEquals(Localized.get(getTranslationKey(Keymap.ITEMPATH)), preferencesMenu.getItems().get(0).getText());

    MenuItem keymapItem = preferencesMenu.getItems().get(0);
    for (int i = 0; i < 10; i++) {
      if (keymapItem.getGraphic() == null) {
        Thread.sleep(100);
      }
    }
    assertNotNull(keymapItem.getGraphic());
    assertEquals(ImageView.class, keymapItem.getGraphic().getClass());
    ImageView view = (ImageView) keymapItem.getGraphic();
    assertSame(Images.get("keymap.jpg"), view.getImage());
  }

  private String getTranslationKey(String menupath) {
    return menupath.substring(1).replace("/", ".");
  }

  @Test
  public void testEventThrowing() throws Exception {
    bus.register(this);
    try {
      MenuBar menu = this.presenter.getMenu("/main/file");
      menu.getMenus().get(0).getItems().get(0).getOnAction().handle(new ActionEvent());
      assertNotNull(eventItem);
      assertEquals(Open.ITEMPATH.toLowerCase(), eventItem.getMenuItemPath());
    } finally {
      bus.unregister(this);
    }
  }

  @Subscribe
  public void onItemClicked(MenuItemClickedEvent event) {
    eventItem = event.getItem();
  }
}
