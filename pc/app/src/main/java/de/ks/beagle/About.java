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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@MenuItem("/main/help")
public class About implements NodeProvider<StackPane> {
  private static final Logger log = LoggerFactory.getLogger(About.class);

  @Override
  public StackPane getNode() {
    return new DefaultLoader<StackPane, Object>(getClass().getResource("about.fxml")).getView();
  }
}
