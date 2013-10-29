package de.ks.cdi.scope;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */
class Extension implements javax.enterprise.inject.spi.Extension {
  private static final Logger log = LogManager.getLogger(Extension.class);

  public void registerContext(@Observes AfterBeanDiscovery event) {
    log.debug("Registering {0}", StackSessionContext.class.getName());
    event.addContext(new StackSessionContext());
  }
}
