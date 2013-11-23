package de.ks.workflow;/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.NodeProvider;
import de.ks.application.fxml.DefaultLoader;
import de.ks.workflow.cdi.WorkflowScoped;
import de.ks.workflow.view.full.FullWorkflowView;
import javafx.scene.Node;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;

/**
 * @param <M> Model class
 */
@WorkflowScoped
@Vetoed
public abstract class Workflow<M, V extends Node, C> implements NodeProvider<V> {
  @Inject
  WorkflowConfig cfg;
  @Inject
  WorkflowNavigator navigator;

  private DefaultLoader<V, C> loader;


  @PostConstruct
  public void initialize() {
    configureSteps();
    loader = new DefaultLoader<>(getViewDefinition());
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
    return loader.getView();
  }

  public C getController() {
    return loader.getController();
  }

  public WorkflowNavigator getNavigator() {
    return navigator;
  }
}