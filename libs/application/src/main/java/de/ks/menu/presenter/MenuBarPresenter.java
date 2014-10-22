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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.ks.i18n.Localized;
import de.ks.imagecache.Images;
import de.ks.menu.MenuItemDescriptor;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 */
public class MenuBarPresenter extends AbstractPresenter<MenuBar> {
  Map<String, Menu> menus = new TreeMap<>();
  List<String> menuNames = new ArrayList<>();
  MenuBar menuBar = null;
  protected final ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).build());

  public MenuBarPresenter() {
    executorService.setCorePoolSize(0);
    executorService.setKeepAliveTime(5, TimeUnit.SECONDS);
  }

  @Override
  public MenuBar getMenu(String menuPath) {
    if (menuBar == null) {
      for (MenuItemDescriptor item : this.menuExtension.getMenuEntries(menuPath)) {
        String currentItemMenuPath = item.getMenuPath();
        menuNames.add(currentItemMenuPath);
        Menu menu = getOrCreateMenu(currentItemMenuPath);

        MenuItem menuItem = createMenuItem(item);
        menu.getItems().add(menuItem);
      }

      List<Menu> rootMenus = createMenuTreeStructure();

      menuBar = new MenuBar();
      menuBar.getMenus().addAll(rootMenus);
    }
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
      Images.later(item.getImagePath(), executorService)//
        .thenAccept(img -> menuItem.setGraphic(new ImageView(img)));
    }
    menuItem.setId(item.getMenuItemPath());
    menuItem.setText(Localized.get(item.getTranslationPath()));
    menuItem.setOnAction(createDefaultActionHandler(item));
    return menuItem;
  }

  private void createMenu(String menuPath) {
    Menu menu = new Menu();
    menu.setId(menuPath);
    menu.setText(Localized.get(menuPath.toLowerCase(Locale.ENGLISH).substring(1).replace("/", ".")));
    menus.put(menuPath, menu);
  }

  protected List<Menu> createMenuTreeStructure() {
    List<String> rootMenus = new ArrayList<>();

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
          String rootName = seperator + menuPath[0] + seperator + menuPath[1];
          if (!rootMenus.contains(rootName)) {
            rootMenus.add(rootName);
          }
        }
        lastMenu = currentMenu;
      }
    }
    List<Menu> retval = rootMenus.stream().map(m -> getOrCreateMenu(m)).collect(Collectors.toList());
    return retval;
  }
}
