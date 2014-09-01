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
import de.ks.validation.ValidationRegistry;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.lang.management.ManagementFactory;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@ActivityScoped
public class ActivityStore {
  static final boolean isDebugging;

  static {
    isDebugging = ManagementFactory.getRuntimeMXBean().getInputArguments().stream().filter(s -> s.contains("jdwp")).findFirst().isPresent();
  }

  static enum LoadOrSave {
    LOAD, SAVE;
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
  @Inject
  ValidationRegistry registry;

  protected final SimpleObjectProperty<Object> model = new SimpleObjectProperty<>();
  protected final SimpleBooleanProperty loading = new SimpleBooleanProperty(false);

  protected final ConcurrentLinkedDeque<LoadOrSave> queue = new ConcurrentLinkedDeque<>();
  protected final AtomicBoolean inExecution = new AtomicBoolean();
  protected DataSource datasource;
  protected volatile CompletableFuture<Void> loadingFuture;
  protected volatile CompletableFuture<Object> savingFuture;

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

  protected boolean advanceInQueue() {
    LoadOrSave first = queue.peekFirst();
    if (first != null && inExecution.compareAndSet(false, true)) {
      syncSetLoadingProperty();
      log.trace("Advanced in queue, next task is {}", first);
      if (first == LoadOrSave.SAVE) {
        doSave();
        return true;
      } else if (first == LoadOrSave.LOAD) {
        doReload();
        return true;
      }
    } else {
      log.trace("Could not advance in queue");
    }
    return false;
  }

  private void syncSetLoadingProperty() {
    try {
      javaFXExecutor.submit(() -> loading.set(true)).get();
    } catch (InterruptedException e) {
      //
    } catch (ExecutionException e) {
      log.error("Coulöd not set loading property", e);
    }
  }

  protected synchronized void finishExecution() {
    queue.removeFirst();
    inExecution.set(false);
    log.trace("Finished execution");
    if (!advanceInQueue()) {
      javaFXExecutor.submit(() -> loading.set(false));
    }
  }

  @SuppressWarnings("unchecked")
  protected void doReload() {
    loadingFuture = null;
    CompletableFuture<Object> load = CompletableFuture.supplyAsync(() -> {
      return datasource.loadModel(m -> {
        if (m != null) {
          initialization.getDataStoreCallbacks().forEach(c -> c.duringLoad(m));
        }
      });
    }, executor);

    boolean isFirstScheduling = load.getNumberOfDependents() == 0;

    if (isFirstScheduling) {
      loadingFuture = load.thenApplyAsync((value) -> {
        log.debug("Loaded model '{}'", value);
        setModel(value);
        return value;
      }, javaFXExecutor).thenAcceptAsync((value) -> {
        try {
          EventBus eventBus = CDI.current().select(EventBus.class).get();
          eventBus.post(new ActivityLoadFinishedEvent(value));
        } finally {
          finishExecution();
        }
      }, javaFXExecutor).exceptionally((t) -> {
        try {
          log.error("Could not load DataSource {} for activity {}", datasource, context.getCurrentActivity(), t);
          return null;
        } finally {
          finishExecution();
        }
      });
    }

  }

  @SuppressWarnings("unchecked")
  protected void doSave() {
    savingFuture = null;
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
        try {
          log.debug("Saved model '{}'", value);
          return value;
        } finally {
          finishExecution();
        }
      }).exceptionally((t) -> {
        try {
          log.error("Could not save model {} DataSource {} for activity {}", model, datasource, context.getCurrentActivity(), t);
          return null;
        } finally {
          finishExecution();
        }
      });
    }
  }

  public void reload() {
    queue.add(LoadOrSave.LOAD);
    advanceInQueue();
  }

  public void save() {
    queue.add(LoadOrSave.SAVE);
    advanceInQueue();
  }

  protected void waitForLoad() {
    waitForFuture(loadingFuture, "Waited too long for loading, will continue.");
  }

  protected void waitForSave() {
    waitForFuture(savingFuture, "Waited too long for saving, will continue.");
  }

  protected void waitForFuture(CompletableFuture<?> future, String msg) {
    if (future == null || future.isDone()) {
      return;
    }
    if (Platform.isFxApplicationThread()) {
      return;
    }
    if (!isDebugging) {
      try {
        future.get(10, TimeUnit.SECONDS);
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

  public void waitForDataSource() {
    waitForLoad();
    waitForSave();
    while (!queue.isEmpty()) {
      waitForLoad();
      waitForSave();
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        //
      }
    }
  }

  public boolean isLoading() {
    return loading.get();
  }

  public ReadOnlyBooleanProperty loadingProperty() {
    return loading;
  }
}
