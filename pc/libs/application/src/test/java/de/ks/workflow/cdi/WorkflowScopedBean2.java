package de.ks.workflow.cdi;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 *
 */
@WorkflowScoped()
public class WorkflowScopedBean2 {
  private static final Logger log = LogManager.getLogger(WorkflowScopedBean2.class);

  public String getName() {
    return getClass().getName();
  }

  @PostConstruct
  public void before() {
    log.debug("PostConstruct {}",WorkflowScopedBean2.class.getSimpleName());
  }

  @PreDestroy
  public void after() {
    log.debug("PreDestroy {}",WorkflowScopedBean2.class.getSimpleName());
  }
}
