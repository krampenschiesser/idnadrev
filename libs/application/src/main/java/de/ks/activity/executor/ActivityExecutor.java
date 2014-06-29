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
package de.ks.activity.executor;

import de.ks.executor.JavaFXExecutorService;
import de.ks.executor.SuspendableExecutorService;
import de.ks.executor.SuspendablePooledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Singleton
public class ActivityExecutor {
  private static final Logger log = LoggerFactory.getLogger(ActivityExecutor.class);

  protected Map<String, SuspendablePooledExecutorService> activityBoundExecutors = new ConcurrentHashMap<>();
  protected Map<String, JavaFXExecutorService> activityBoundFXExecutors = new ConcurrentHashMap<>();

  public SuspendablePooledExecutorService getActivityExecutorService(String id) {
    return getServiceInternal(id, () -> new SuspendablePooledExecutorService(id), activityBoundExecutors);
  }

  public JavaFXExecutorService getJavaFXExecutorService(String id) {
    return getServiceInternal(id, JavaFXExecutorService::new, activityBoundFXExecutors);
  }

  protected <T extends SuspendableExecutorService> T getServiceInternal(String id, Supplier<T> supplier, Map<String, T> map) {
    map.putIfAbsent(id, supplier.get());
    T service = map.get(id);
    if (service.isShutdown()) {
      log.debug("Old executor {} for activity is shutDown, will start new", service.getClass().getSimpleName(), id);
      map.put(id, supplier.get());
      service = map.get(id);
    }
    return service;
  }

  public void startOrResume(String id) {
    SuspendableExecutorService service = getActivityExecutorService(id);
    log.debug("{} executor service {}", service.isSuspended() ? "Resuming" : "Starting", id);
    service.resume();

    service = getJavaFXExecutorService(id);
    log.debug("{} JavaFX executor service {}", service.isSuspended() ? "Resuming" : "Starting", id);
    service.resume();
  }

  public void suspend(String id) {
    SuspendableExecutorService service = getActivityExecutorService(id);
    log.debug("Suspending executor service {}", id);
    service.suspend();
    service.waitForAllTasksDoneAndDrain();

    service = getJavaFXExecutorService(id);
    log.debug("Suspending JavaFX executor service {}", id);
    service.suspend();
    service.waitForAllTasksDoneAndDrain();
  }

  public void shutdown(String id) {
    SuspendableExecutorService service = getActivityExecutorService(id);
    log.debug("Shutting down executor service {}", id);
    service.shutdownNow();
    service.waitForAllTasksDoneAndDrain();

    service = getJavaFXExecutorService(id);
    log.debug("Shutting down JavaFX executor service {}", id);
    service.shutdownNow();
    service.waitForAllTasksDoneAndDrain();
  }

  public void shutdownAll() {
    shutdownAll(activityBoundExecutors);
    shutdownAll(activityBoundFXExecutors);
  }

  protected void shutdownAll(Map<String, ? extends SuspendableExecutorService> map) {
    for (Map.Entry<String, ? extends SuspendableExecutorService> entry : map.entrySet()) {
      SuspendableExecutorService executor = entry.getValue();
      List<Runnable> runnables = executor.shutdownNow();
      log.debug("{} runnables remaining after shutdown of activity executor '{}'", runnables.size(), entry.getKey());
    }
    for (Map.Entry<String, ? extends SuspendableExecutorService> entry : map.entrySet()) {
      String activityName = entry.getKey();
      SuspendableExecutorService executor = entry.getValue();
      try {
        executor.awaitTermination(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        log.error("Unable to stop executor for activity {}. {} active tasks.", activityName, executor.getActiveCount());
      }
    }
  }
}
