package de.ks.workflow;/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.cdi.WorkflowScoped;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;

/**
 * @param <M> Model class
 */
@WorkflowScoped
@Vetoed
public abstract class Workflow<M> {
  @Inject
  WorkflowConfig cfg;

  @PostConstruct
  public void initialize() {
    configureSteps();
  }

  public abstract  M getModel();

  public  String getName() {
    return getClass().getSimpleName();
  }

  protected abstract void configureSteps();
}
