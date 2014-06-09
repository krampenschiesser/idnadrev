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

import de.ks.LauncherRunner;
import de.ks.menu.mainmenu.Open;
import de.ks.menu.mainmenu.Save;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.CDI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 */
@RunWith(LauncherRunner.class)
public class MenuItemDiscovererTest {
  @Test
  public void testDiscoverItems() throws Exception {
    MenuExtension extension = CDI.current().select(MenuExtension.class).get();
    assertEquals(4, extension.getMenuEntries().size());

    assertEquals(4, extension.getMenuEntries("/main").size());
    assertEquals(2, extension.getMenuEntries("/main/file").size());
  }

  @Test
  public void testInstanceIteration() throws Exception {
    MenuExtension extension = CDI.current().select(MenuExtension.class).get();
    Collection<Class<?>> items = extension.getMenuClasses();

    List<Class<?>> classes = new ArrayList<>();
    items.forEach((Object item) -> {
      classes.add(item.getClass());
    });
    assertEquals(4, classes.size());
  }

  @Test
  public void testMenuFilter() throws Exception {
    List<Class<?>> fileMenu = new ArrayList<>();
    List<Class<?>> mainMenu = new ArrayList<>();

    MenuExtension extension = CDI.current().select(MenuExtension.class).get();
    Collection<Class<?>> items = extension.getMenuClasses();
    items.forEach(new MenuFilter("/main/file", fileMenu));
    items.forEach(new MenuFilter("/main", mainMenu));

    assertEquals(2, fileMenu.size());
    assertEquals(4, mainMenu.size());
    assertEquals(Open.class, fileMenu.get(0));
    assertEquals(Save.class, fileMenu.get(1));
  }
}
