/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
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

package de.ks.activity.context;

import de.ks.binding.Binding;
import de.ks.datasource.DataSource;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 *
 */
@ActivityScoped
public class ActivityStore {
  private static final Logger log = LoggerFactory.getLogger(ActivityStore.class);

  private final SimpleObjectProperty<Object> model = new SimpleObjectProperty<>();
  @Inject
  private Binding binding;
  private DataSource<?> datasource;

  @PostConstruct
  public void initialize() {
    model.addListener(binding::bindChangedModel);
  }

  @SuppressWarnings("unchecked")
  public <E> E getModel() {
    return (E) model.get();
  }

  public void setModel(Object model) {
    log.info("Setting new model {}", model);
    this.model.set(null);
    this.model.set(model);
  }

  public SimpleObjectProperty<?> getModelProperty() {
    return model;
  }

  public Binding getBinding() {
    return binding;
  }

  public void setDatasource(DataSource<?> datasource) {
    this.datasource = datasource;
  }

  public DataSource<?> getDatasource() {
    return datasource;
  }
}
