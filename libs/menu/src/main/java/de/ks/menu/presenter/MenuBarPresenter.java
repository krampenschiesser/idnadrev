package de.ks.menu.presenter;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.ks.i18n.Localized;
import de.ks.imagecache.Images;
import de.ks.menu.MenuItemDescriptor;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Collection;

/**
 *
 */
public class MenuBarPresenter extends AbstractPresenter<MenuBar> {
  Multimap<String, Menu> menues = HashMultimap.create();

  @Override
  public MenuBar getMenu(String menuPath) {
    for (MenuItemDescriptor item : this.menuExtension.getMenuEntries(menuPath)) {
      String currentItemMenuPath = item.getMenuPath();
      if (!menues.containsKey(currentItemMenuPath)) {
        createMenu(currentItemMenuPath);
      }
      Collection<Menu> menu = menues.get(menuPath);

      MenuItem menuItem = new MenuItem();
      if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
        Images.later(item.getImagePath(), (Image img) -> menuItem.setGraphic(new ImageView(img)));
      }
    }

    MenuBar menuBar = new MenuBar();
    menuBar.getMenus().addAll(menues.values());
    return menuBar;
  }

  private void createMenu(String menuPath) {
    Menu menu = new Menu();
    menu.setText(Localized.get(menuPath));
    menues.put(menuPath, menu);
  }
}
