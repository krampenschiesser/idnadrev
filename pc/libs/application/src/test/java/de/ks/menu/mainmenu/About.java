package de.ks.menu.mainmenu;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.menu.MenuItem;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 *
 */
@MenuItem(About.MENUPATH)
public class About extends StackPane {
  public static final String MENUPATH = "/main/options";
  public static final String ITEMPATH = MENUPATH + "/" + About.class.getSimpleName().toLowerCase();

  public About() {
    getChildren().add(new Label("hello world"));
  }
}
