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
public class WorkflowScopedBean1 {
  private static final Logger log = LogManager.getLogger(WorkflowScopedBean1.class);
  protected String value;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getName() {
    return getClass().getName();
  }

  @PostConstruct
  public void before() {
    log.debug("PostConstruct {}",WorkflowScopedBean1.class.getSimpleName());
  }

  @PreDestroy
  public void after() {
    log.debug("PreDestroy {}",WorkflowScopedBean1.class.getSimpleName());
  }
}
