package de.ks.workflow.cdi;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

/**
 *
 */
class StoredBean {
  final Bean bean;
  final CreationalContext creationalContext;
  final Object instance;

  StoredBean(Bean<?> bean, CreationalContext<?> creationalContext, Object instance) {
    this.bean = bean;
    this.creationalContext = creationalContext;
    this.instance = instance;
  }

  Bean<?> getBean() {
    return bean;
  }

  CreationalContext<?> getCreationalContext() {
    return creationalContext;
  }

  @SuppressWarnings("unchecked")
  <T> T getInstance() {
    return (T) instance;
  }

  public void destroy() {
    bean.destroy(instance,creationalContext);
  }
}
