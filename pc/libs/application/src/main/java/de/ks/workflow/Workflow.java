/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ks.workflow;

import de.ks.NodeProvider;
import de.ks.application.fxml.DefaultLoader;
import de.ks.workflow.cdi.WorkflowScoped;
import de.ks.workflow.navigation.WorkflowNavigator;
import de.ks.workflow.view.full.FullWorkflowView;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;

/**
 * @param <M> Model class
 */
@WorkflowScoped
@Vetoed
public abstract class Workflow<M, V extends Node, C> implements NodeProvider<V> {
  private static final Logger log = LoggerFactory.getLogger(Workflow.class);
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
