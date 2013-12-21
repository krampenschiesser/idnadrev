package de.ks.workflow;/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.NodeProvider;
import de.ks.application.fxml.DefaultLoader;
import de.ks.workflow.cdi.WorkflowScoped;
import de.ks.workflow.navigation.WorkflowNavigator;
import de.ks.workflow.view.full.FullWorkflowView;
import javafx.scene.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;

/**
 * @param <M> Model class
 */
@WorkflowScoped
@Vetoed
public abstract class Workflow<M, V extends Node, C> implements NodeProvider<V> {
  private static final Logger log = LogManager.getLogger(Workflow.class);
  @Inject
  protected WorkflowConfig cfg;
  @Inject
  protected WorkflowNavigator navigator;

  private DefaultLoader<V, C> loader;


  @PostConstruct
  public void initialize() {
    configureSteps();
    if (cfg.getRoot() == null) {
      throw new RuntimeException("Could not initialize steps for workfllw " + getName());
    }
    log.debug("Configured {} steps for workflow {}", cfg.getStepList().size(), getName());
  }

  public abstract M getModel();

  public abstract Class<M> getModelClass();

  public String getName() {
    return getClass().getSimpleName();
  }

  protected abstract void configureSteps();

  protected Class<?> getViewDefinition() {
    return FullWorkflowView.class;
  }

  @Override
  public V getNode() {
    return getLoader().getView();
  }

  public C getController() {
    return getLoader().getController();
  }

  public WorkflowNavigator getNavigator() {
    return navigator;
  }

  public void waitForInitialization() {
    getLoader().getView();
  }

  public WorkflowConfig getCfg() {
    return cfg;
  }

  public DefaultLoader<V, C> getLoader() {
    if (loader == null) {
      loader = new DefaultLoader<>(getViewDefinition());
    }
    return loader;
  }
}
