package de.ks.menu.presenter;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.i18n.Localized;
import de.ks.imagecache.Images;
import de.ks.menu.MenuItemDescriptor;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.*;

/**
 *
 */
public class MenuBarPresenter extends AbstractPresenter<MenuBar> {
  Map<String, Menu> menus = new TreeMap<>();

  @Override
  public MenuBar getMenu(String menuPath) {
    for (MenuItemDescriptor item : this.menuExtension.getMenuEntries(menuPath)) {
      String currentItemMenuPath = item.getMenuPath();
      Menu menu = getOrCreateMenu(currentItemMenuPath);

      MenuItem menuItem = createMenuItem(item);
      menu.getItems().add(menuItem);
    }

    List<Menu> rootMenus = createMenuTreeStructure();

    MenuBar menuBar = new MenuBar();
    menuBar.getMenus().addAll(rootMenus);
    return menuBar;
  }

  private Menu getOrCreateMenu(String currentItemMenuPath) {
    if (!menus.containsKey(currentItemMenuPath)) {
      createMenu(currentItemMenuPath);
    }
    return menus.get(currentItemMenuPath);
  }

  private MenuItem createMenuItem(MenuItemDescriptor item) {
    MenuItem menuItem = new MenuItem();
    if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
      Images.later(item.getImagePath(), (Image img) -> menuItem.setGraphic(new ImageView(img)));
    }
    menuItem.setText(Localized.get(item.getMenuItemPath()));
    return menuItem;
  }

  private void createMenu(String menuPath) {
    Menu menu = new Menu();
    menu.setText(Localized.get(menuPath));
    menus.put(menuPath, menu);
  }

  protected List<Menu> createMenuTreeStructure() {
    TreeSet<String> rootMenus = new TreeSet<>();
    TreeSet<String> menuNames = new TreeSet<>(menus.keySet());

    for (String menuName : menuNames) {
      String seperator = de.ks.menu.MenuItem.SEPERATOR;
      String[] menuPath = menuName.substring(1).split(seperator);
      String currentMenuName = seperator + menuPath[0];
      Menu lastMenu = null;
      for (int i = 1; i < menuPath.length; i++) {
        currentMenuName += seperator + menuPath[i];
        Menu currentMenu = getOrCreateMenu(currentMenuName);
        if (lastMenu != null) {
          lastMenu.getItems().add(currentMenu);
        }
        if (i == 1) {
          rootMenus.add(seperator + menuPath[0] + seperator + menuPath[1]);
        }
        lastMenu = currentMenu;
      }
    }
    ArrayList<Menu> retval = new ArrayList<>();
    for (String rootMenu : rootMenus) {
      retval.add(getOrCreateMenu(rootMenu));
    }
    return retval;
  }
}
