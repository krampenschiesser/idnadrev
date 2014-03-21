/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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

package de.ks.menu;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class MenuExtension implements Extension {
  private static final Logger log = LoggerFactory.getLogger(MenuExtension.class);

  protected final TreeMap<String, MenuItemDescriptor> menuEntries = new TreeMap<>();

  public void onBean(@Observes ProcessBean event) {
    if (event.getAnnotated().isAnnotationPresent(MenuItem.class)) {
      MenuItem annotation = event.getAnnotated().getAnnotation(MenuItem.class);

      MenuItemDescriptor menuItemDescriptor = new MenuItemDescriptor(annotation, event.getBean().getBeanClass());
      menuEntries.put(menuItemDescriptor.getMenuItemPath(), menuItemDescriptor);
    }
    log.debug("Discovered bean {}", event.getBean().getBeanClass().getName());
  }

  public TreeMap<String, MenuItemDescriptor> getMenuEntries() {
    return menuEntries;
  }

  public List<MenuItemDescriptor> getMenuEntries(String menuPath) {
    return menuEntries.values().parallelStream()//
            .filter((MenuItemDescriptor item) -> item.getMenuItemPath().startsWith(menuPath))//
            .sorted()//
            .collect(Collectors.toList());
  }

  public List<Class<?>> getMenuClasses() {
    return getMenuEntries().values().stream().map(MenuItemDescriptor::getTarget).collect(Collectors.toList());
  }
}
