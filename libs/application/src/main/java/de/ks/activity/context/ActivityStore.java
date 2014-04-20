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

package de.ks.activity.context;


import de.ks.binding.Binding;
import de.ks.datasource.DataSource;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 */
@ActivityScoped
public class ActivityStore {
  private final SimpleObjectProperty<Object> model = new SimpleObjectProperty<>();
  private final Binding binding = new Binding();
  private DataSource<?> datasource;

  public ActivityStore() {
    model.addListener(binding::bindChangedModel);
  }

  @SuppressWarnings("unchecked")
  public <E> E getModel() {
    return (E) model.get();
  }

  public void setModel(Object model) {
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

  @SuppressWarnings("unchecked")
  public <M> DataSource<M> getDatasource() {
    return (DataSource<M>) datasource;
  }
}
