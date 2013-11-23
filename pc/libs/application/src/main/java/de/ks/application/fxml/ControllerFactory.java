package de.ks.application.fxml;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javafx.util.Callback;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.lang.annotation.Annotation;

/**
 *
 */
public class ControllerFactory implements Callback<Class<?>, Object> {
  @Override
  public Object call(Class<?> clazz) {
    BeanManager beanManager = CDI.current().getBeanManager();
    for (Annotation annotation : clazz.getAnnotations()) {
      if (beanManager.isScope(annotation.annotationType())) {
        throw new IllegalStateException("Class " + clazz.getName() + " is not allowed to be in scope " + annotation + " because JavaFX can't inject fields in proxy types");
      }
    }

    return CDI.current().select(clazz).get();
  }
}
