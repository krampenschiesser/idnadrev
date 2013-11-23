package de.ks.application.fxml;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.i18n.Localized;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.util.Callback;

/**
 *
 */
public class DefaultLoader {
  public static
  public static <T> T load(String fileName) {
    FXMLLoader.load(getClass().getResource("about.fxml"), Localized.getBundle(), new JavaFXBuilderFactory(), new Callback<Class<?>, Object>();
    return null;
  }
}
