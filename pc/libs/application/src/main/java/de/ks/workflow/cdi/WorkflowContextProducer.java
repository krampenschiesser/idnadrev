package de.ks.workflow.cdi;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.CDI;

public class WorkflowContextProducer {
  @Produces
  @Default
  public WorkflowContext getContext() {
    return (WorkflowContext) CDI.current().getBeanManager().getContext(WorkflowScoped.class);
  }
}
