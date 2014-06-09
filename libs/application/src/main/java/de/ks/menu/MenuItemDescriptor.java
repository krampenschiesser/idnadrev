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

package de.ks.menu;

/**
 *
 */
public class MenuItemDescriptor implements Comparable<MenuItemDescriptor> {
  protected final String menuPath;
  protected final String menuItemPath;
  protected final String imagePath;
  protected final Class<?> target;

  public MenuItemDescriptor(MenuItem menu, Class<?> target) {
    this(menu.value(), menu.image(), target);
  }

  public MenuItemDescriptor(String menuPath, Class<?> target) {
    this(menuPath, null, target);
  }

  public MenuItemDescriptor(String menuPath, String imagePath, Class<?> target) {
    this.menuPath = menuPath;
    this.menuItemPath = menuPath + "/" + target.getSimpleName().toLowerCase();
    this.imagePath = imagePath;
    this.target = target;
  }

  @Override
  public int compareTo(MenuItemDescriptor o) {
    return menuItemPath.compareTo(o.menuItemPath);
  }

  public String getMenuPath() {
    return menuPath;
  }

  public String getMenuItemPath() {
    return menuItemPath;
  }

  public String getTranslationPath() {
    return menuItemPath.substring(1).replace("/", ".");
  }

  public String getImagePath() {
    return imagePath;
  }

  public Class<?> getTarget() {
    return target;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MenuItemDescriptor)) {
      return false;
    }

    MenuItemDescriptor that = (MenuItemDescriptor) o;

    if (menuItemPath != null ? !menuItemPath.equals(that.menuItemPath) : that.menuItemPath != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return menuItemPath != null ? menuItemPath.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "MenuItemDescriptor{" +
            "menuItemPath='" + menuItemPath + '\'' +
            ", target=" + target +
            '}';
  }
}
