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
package de.ks.executor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.ks.util.LockSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class SuspendablePooledExecutorService extends ScheduledThreadPoolExecutor {
  private static final Logger log = LoggerFactory.getLogger(SuspendablePooledExecutorService.class);

  public static final int COMPLETABLE_FUTURES_OFFSET = 10;//some currently running tasks may specify a thenRun
  private final String name;
  protected volatile boolean suspended = false;
  protected final ReentrantLock lock = new ReentrantLock();
  private final ArrayList<Runnable> suspendedTasks = new ArrayList<>();

  public SuspendablePooledExecutorService(String name) {
    //using 1 core thread sometime stops the executor to start new threads handling an async future
    //if we poll for that future in the only runnign thread->deadlock... big time
    this(name, Math.max(4, Runtime.getRuntime().availableProcessors()), Runtime.getRuntime().availableProcessors() * 4);
  }

  public SuspendablePooledExecutorService(String name, int corePoolSize, int maximumPoolSize) {
    super(corePoolSize, new ThreadFactoryBuilder().setDaemon(true).setNameFormat(name + "-%d").setUncaughtExceptionHandler(new LoggingUncaughtExceptionHandler()).build());
    setMaximumPoolSize(maximumPoolSize);
    setKeepAliveTime(1, TimeUnit.MINUTES);
    this.name = name;
  }

  public void suspend() {
    if (isSuspended()) {
      return;
    }
    try (LockSupport support = new LockSupport(lock)) {
      suspended = true;
      suspendedTasks.clear();
      drainTasks();
    }
    waitForAllTasksDoneAndDrain();
  }

  protected void drainTasks() {
    try (LockSupport support = new LockSupport(lock)) {
      suspendedTasks.ensureCapacity(getQueue().size() + COMPLETABLE_FUTURES_OFFSET);
      getQueue().drainTo(suspendedTasks);
    }
  }

  public void waitForAllTasksDoneAndDrain() {
    while (getActiveCount() > 0 || !getQueue().isEmpty()) {
      drainTasks();
      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException e) {
        log.trace("Got interrupted while waiting for tasks.", e);
      }
    }
  }

  public void waitForAllTasksDone() {
    while (getActiveCount() > 0 || !getQueue().isEmpty()) {
      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException e) {
        log.trace("Got interrupted while waiting for tasks.", e);
      }
    }
  }

  @Override
  public void execute(Runnable command) {
    if (isSuspended()) {
      try (LockSupport support = new LockSupport(lock)) {
        suspendedTasks.add(command);
      }
    } else {
      super.execute(command);
    }
  }

  public void resume() {
    try (LockSupport support = new LockSupport(lock)) {
      suspended = false;
      suspendedTasks.forEach(r -> execute(r));
//      getQueue().addAll(suspendedTasks);
    }
  }

  public ArrayList<Runnable> getSuspendedTasks() {
    return suspendedTasks;
  }

  public boolean isSuspended() {
    return suspended;
  }

  public String getName() {
    return name;
  }
}
