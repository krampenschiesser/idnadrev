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
@MenuItem(About.MENUPATH)
public class About {
  public static final String MENUPATH = "/main/options";
  public static final String ITEMPATH = MENUPATH + "/" + About.class.getSimpleName();
}
