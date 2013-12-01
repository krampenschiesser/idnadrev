package de.ks.menu.mainmenu;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.menu.MenuItem;

/**
 *
 */
@MenuItem(Save.MENUPATH)
public class Save {
  public static final String MENUPATH = "/main/file";
  public static final String ITEMPATH = MENUPATH + "/" + Save.class.getSimpleName().toLowerCase();
}
