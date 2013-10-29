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

@MenuItem(value = Keymap.MENUPATH, image = "keymap.jpg")
public class Keymap {
  public static final String MENUPATH = "/main/options/preferences";
  public static final String ITEMPATH = MENUPATH + "/" + Keymap.class.getSimpleName().toLowerCase();
}
