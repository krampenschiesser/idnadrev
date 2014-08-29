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

import de.ks.activity.ActivityLoadFinishedEvent;
import de.ks.activity.executor.ActivityExecutor;
import de.ks.activity.executor.ActivityJavaFXExecutor;
import de.ks.activity.initialization.ActivityInitialization;
import de.ks.binding.Binding;
import de.ks.datasource.DataSource;
import de.ks.eventsystem.bus.EventBus;
import de.ks.executor.group.LastExecutionGroup;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
@ActivityScoped
public class ActivityStore {
  private static final Logger log = LoggerFactory.getLogger(ActivityStore.class);

  @Inject
  protected Binding binding;
  @Inject
  protected ActivityExecutor executor;
  @Inject
  protected ActivityJavaFXExecutor javaFXExecutor;
  @Inject
  protected ActivityContext context;
  @Inject
  protected ActivityInitialization initialization;

  protected final SimpleObjectProperty<Object> model = new SimpleObjectProperty<>();
  protected DataSource datasource;
  protected LastExecutionGroup loadingGroup;
  protected LastExecutionGroup savingGroup;
  protected volatile CompletableFuture<Void> loadingFuture;
  protected volatile CompletableFuture<Object> savingFuture;

  @PostConstruct
  public void initialize() {
    loadingGroup = new LastExecutionGroup<>(10, executor);
    savingGroup = new LastExecutionGroup<>(10, executor);
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

  @SuppressWarnings("unchecked")
  public void reload() {
    waitForSave();

    CompletableFuture<Object> load = loadingGroup.schedule(() -> datasource.loadModel(m -> {
      if (m != null) {
        initialization.getDataStoreCallbacks().forEach(c -> c.duringLoad(m));
      }
    }));

    boolean isFirstScheduling = load.getNumberOfDependents() == 0;
    if (isFirstScheduling) {

      loadingFuture = load.thenApplyAsync((value) -> {
        log.debug("Loaded model '{}'", value);
        setModel(value);
        return value;
      }, javaFXExecutor).thenAcceptAsync((value) -> {
        EventBus eventBus = CDI.current().select(EventBus.class).get();
        eventBus.post(new ActivityLoadFinishedEvent(value));
      }, javaFXExecutor).exceptionally((t) -> {
        log.error("Could not load DataSource {} for activity {}", datasource, context.getCurrentActivity(), t);
        return null;
      });
    }
  }

  @SuppressWarnings("unchecked")
  public void save() {
    waitForLoad();

    Object model = getModel();
    CompletableFuture<Object> save = CompletableFuture.supplyAsync(() -> {
      log.debug("Start saving model");
      datasource.saveModel(model, m -> {
        getBinding().applyControllerContent(m);
        initialization.getDataStoreCallbacks().forEach(c -> c.duringSave(m));
      });
      log.debug("Initially saved model '{}'", model);
      return model;
    }, executor);


    boolean isFirstScheduling = save.getNumberOfDependents() == 0;
    if (isFirstScheduling) {

      savingFuture = save.thenApply((value) -> {
        log.debug("Saved model '{}'", value);
        return value;
      }).exceptionally((t) -> {
        log.error("Could not save model {} DataSource {} for activity {}", model, datasource, context.getCurrentActivity(), t);
        return null;
      });
    }
  }

  public void waitForLoad() {
    if (loadingFuture == null || loadingFuture.isDone()) {
      return;
    }
    if (Platform.isFxApplicationThread()) {
      return;
    }
    loadingFuture.join();
  }

  public void waitForSave() {
    if (savingFuture == null || savingFuture.isDone()) {
      return;
    }
    if (Platform.isFxApplicationThread()) {
      return;
    }
    savingFuture.join();
  }

  public void waitForDataSource() {
    waitForLoad();
    waitForSave();
  }
}
