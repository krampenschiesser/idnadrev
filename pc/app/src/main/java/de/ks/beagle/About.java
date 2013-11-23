package de.ks.beagle;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.NodeProvider;
import de.ks.application.fxml.DefaultLoader;
import de.ks.menu.MenuItem;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 */
@MenuItem("/main/help")
public class About implements NodeProvider<StackPane> {
  private static final Logger log = LogManager.getLogger(About.class);

  @Override
  public StackPane getNode() {
    return new DefaultLoader<StackPane, Object>(getClass().getResource("about.fxml")).getView();
  }
}
