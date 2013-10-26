package de.ks.cdi.scope;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */
public class StackSessionContext implements Context {
  public static final StackSessionContext instance = new StackSessionContext();

  private final Map<String, Map<Class<?>, Object>> store = new HashMap<>();

  @Override
  public Class<? extends Annotation> getScope() {
    return StackSessionScope.class;
  }

  @Override
  public <T> T get(Contextual<T> tContextual) {
    return null;
  }

  @Override
  public <T> T get(Contextual<T> tContextual, CreationalContext<T> tCreationalContext) {
    return null;
  }

  @Override
  public boolean isActive() {
    return true;
  }
}
