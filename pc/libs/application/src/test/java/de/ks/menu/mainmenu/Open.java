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
@MenuItem(Open.MENUPATH)
public class Open {
  public static final String MENUPATH = "/main/file";
  public static final String ITEMPATH = MENUPATH + "/" + Open.class.getSimpleName().toLowerCase();
}
