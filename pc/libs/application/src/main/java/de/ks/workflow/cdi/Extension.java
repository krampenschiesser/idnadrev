package de.ks.workflow.cdi;

import de.ks.executor.ExecutorService;
import de.ks.executor.ThreadPropagations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */
public class Extension implements javax.enterprise.inject.spi.Extension {
  private static final Logger log = LoggerFactory.getLogger(Extension.class);

  public void registerContext(@Observes AfterBeanDiscovery event, BeanManager mgr) {
    log.debug("Registering {}", WorkflowContext.class.getName());
    WorkflowContext context = new WorkflowContext(mgr);
    event.addContext(context);

    ThreadPropagations propagations = ExecutorService.instance.getPropagations();
    propagations.register(new WorkflowPropagator(context));
  }

}
