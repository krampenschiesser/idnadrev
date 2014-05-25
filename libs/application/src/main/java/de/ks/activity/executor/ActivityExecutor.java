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
package de.ks.activity.executor;

import de.ks.executor.SuspendablePooledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Singleton
public class ActivityExecutor {
  private static final Logger log = LoggerFactory.getLogger(ActivityExecutor.class);

  protected Map<String, SuspendablePooledExecutorService> activityBoundExecutors = new ConcurrentHashMap<>();

  public SuspendablePooledExecutorService getActivityExecutorService(String id) {
    activityBoundExecutors.putIfAbsent(id, new SuspendablePooledExecutorService(id));

    SuspendablePooledExecutorService service = activityBoundExecutors.get(id);
    if (service.isShutdown()) {
      log.debug("Old executor for activity is shutDown, will start new", id);
      activityBoundExecutors.put(id, new SuspendablePooledExecutorService(id));
      service = activityBoundExecutors.get(id);
    }
    return service;
  }

  public void startOrResume(String id) {
    SuspendablePooledExecutorService service = getActivityExecutorService(id);
    log.debug("Starting/Resuming executor service {}", service.getName());
    service.resume();
  }

  public void suspend(String id) {
    SuspendablePooledExecutorService service = getActivityExecutorService(id);
    log.debug("Suspending executor service {}", service.getName());
    service.suspend();
  }

  public void shutdown(String id) {
    SuspendablePooledExecutorService service = getActivityExecutorService(id);
    log.debug("Shutting down executor service {}", service.getName());
    service.shutdownNow();
    service.waitForAllTasksDone();
  }

  public void shutdownAll() {
    for (SuspendablePooledExecutorService executor : activityBoundExecutors.values()) {
      List<Runnable> runnables = executor.shutdownNow();
      log.debug("{} runnables remaining after shutdown of activity executor '{}'", runnables.size(), executor.getName());
    }
    for (Map.Entry<String, SuspendablePooledExecutorService> entry : activityBoundExecutors.entrySet()) {
      String activityName = entry.getKey();
      SuspendablePooledExecutorService executor = entry.getValue();
      try {
        executor.awaitTermination(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        log.error("Unable to stop executor for activity {}. {} active tasks.", activityName, executor.getActiveCount());
      }
    }
  }
}
