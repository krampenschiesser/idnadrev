package de.ks.menu;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.menu.mainmenu.Open;
import de.ks.menu.mainmenu.Save;
import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 */
public class MenuItemDiscovererTest {
  private CdiContainer cdiContainer;

  @Before
  public void setUp() throws Exception {
    cdiContainer = CdiContainerLoader.getCdiContainer();
    cdiContainer.boot(null);
    cdiContainer.boot();
  }

  @After
  public void tearDown() throws Exception {
    cdiContainer.shutdown();
  }

  @Test
  public void testDiscoverItems() throws Exception {
    MenuExtension extension = CDI.current().select(MenuExtension.class).get();
    assertEquals(3, extension.getMenuEntries().size());

    assertEquals(3, extension.getMenuEntries("/main").size());
    assertEquals(2, extension.getMenuEntries("/main/file").size());
  }

  @Test
  public void testInstanceIteration() throws Exception {
    Instance<Object> select = CDI.current().select(new MenuItemLiteral());
    assertFalse("nothing found for menuLiteral", select.isUnsatisfied());

    List<Class<?>> classes = new ArrayList<>();
    select.forEach((Object item) -> {
      classes.add(item.getClass());
    });
    assertEquals(3, classes.size());
  }

  @Test
  public void testMenuFilter() throws Exception {
    List<Object> fileMenu = new ArrayList<>();
    List<Object> mainMenu = new ArrayList<>();
    Instance<Object> select = CDI.current().select(new MenuItemLiteral());
    select.forEach(new MenuFilter("/main/file", fileMenu));
    select.forEach(new MenuFilter("/main", mainMenu));

    assertEquals(2, fileMenu.size());
    assertEquals(3, mainMenu.size());
    assertEquals(Open.class, fileMenu.get(0).getClass());
    assertEquals(Save.class, fileMenu.get(1).getClass());
  }
}
