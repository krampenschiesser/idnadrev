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
import de.ks.util.LockSupport;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.lang.management.ManagementFactory;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@ActivityScoped
public class ActivityStore {
  static final boolean isDebugging;

  static {
    isDebugging = ManagementFactory.getRuntimeMXBean().getInputArguments().stream().filter(s -> s.contains("jdwp")).findFirst().isPresent();
  }

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
  protected final ReentrantLock lock = new ReentrantLock();
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
    try (LockSupport support = new LockSupport(lock)) {

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
  }

  @SuppressWarnings("unchecked")
  public void save() {
    waitForLoad();
    try (LockSupport support = new LockSupport(lock)) {
      Object model = getModel();
      CompletableFuture<Object> save = CompletableFuture.supplyAsync(() -> {
        log.error("Start saving model");
        datasource.saveModel(model, m -> {
          getBinding().applyControllerContent(m);
          initialization.getDataStoreCallbacks().forEach(c -> c.duringSave(m));
        });
        log.error("Initially saved model '{}'", model);
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
  }

  public void waitForLoad() {
    waitForFuture(loadingFuture, "Waited too long for loading, will continue.");
  }

  public void waitForSave() {
    waitForFuture(savingFuture, "Waited too long for saving, will continue.");
  }

  protected void waitForFuture(CompletableFuture<?> future, String msg) {
    try (LockSupport support = new LockSupport(lock)) {

      if (future == null || future.isDone()) {
        return;
      }
      if (Platform.isFxApplicationThread()) {
        return;
      }
      if (!isDebugging) {
        try {
          future.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
          //
        } catch (ExecutionException e) {
          throw new RuntimeException(e.getCause());
        } catch (TimeoutException e) {

          log.warn(msg);
        }
      } else {
        try {
          future.join();
        } catch (CancellationException e) {
          //ok
        }
      }
    }
  }

  public void waitForDataSource() {
    waitForLoad();
    waitForSave();
  }
}
