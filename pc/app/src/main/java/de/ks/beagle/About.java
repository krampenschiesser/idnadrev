package de.ks.beagle;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.NodeProvider;
import de.ks.i18n.Localized;
import de.ks.menu.MenuItem;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.enterprise.inject.spi.CDI;
import java.io.IOException;

/**
 *
 */
@MenuItem("/main/help")
public class About implements NodeProvider<StackPane>{
  private static final Logger log = LogManager.getLogger(About.class);

  @Override
  public StackPane get() {
    try {
      StackPane load = FXMLLoader.load(getClass().getResource("about.fxml"), Localized.getBundle(), new JavaFXBuilderFactory(),new Callback<Class<?>, Object>() {
        @Override
        public Object call(Class<?> clazz) {
          return CDI.current().select(clazz).get();
        }
      });
      return load;
    } catch (IOException e) {
      log.error("Could not load about.fxml", e);
      return null;
    }
  }
}
