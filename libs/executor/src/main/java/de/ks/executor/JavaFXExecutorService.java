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

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Vetoed;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

@Vetoed
public class JavaFXExecutorService extends AbstractExecutorService implements SuspendableExecutorService {
  private static final Logger log = LoggerFactory.getLogger(JavaFXExecutorService.class);
  final ExecutorService mock;

  protected final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
  protected volatile boolean suspended = false;
  protected volatile boolean shutdown = false;
  protected final AtomicReference<QueueListener> queueListener = new AtomicReference<>();

  public JavaFXExecutorService() {
    this(null);
  }

  public JavaFXExecutorService(ExecutorService mock) {
    this.mock = mock;
  }

  @Override
  public void shutdown() {
    shutdown = true;
    waitForAllTasksDoneAndDrain();
  }

  @Override
  public List<Runnable> shutdownNow() {
    shutdown = true;
    waitForAllTasksDoneAndDrain();
    return Collections.emptyList();
  }

  @Override
  public boolean isShutdown() {
    return shutdown;
  }

  @Override
  public boolean isTerminated() {
    return false;
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return false;
  }

  @Override
  public void execute(Runnable command) {
    if (isCurrentThread()) {
      command.run();
    } else {
      queue.add(command);
      triggerQueueReading();
    }
  }

  private boolean isCurrentThread() {
    if (mock != null) {
      return false;
    } else {
      return Platform.isFxApplicationThread();
    }
  }

  protected void triggerQueueReading() {
    QueueListener listener = queueListener.get();
    if (listener == null || !listener.isRunning()) {
      listener = queueListener.updateAndGet(l -> l != null && l.isRunning() ? l : new QueueListener(this));
      if (mock != null) {
        mock.execute(listener);
      } else {
        Platform.runLater(listener);
      }
    }
  }

  public <T> T invokeInJavaFXThread(Callable<T> callable) {
    try {
      return submit(callable).get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Could not get value from {}", callable, e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void suspend() {
    suspended = true;
    waitForAllTasksDone();
  }

  @Override
  public void resume() {
    suspended = false;
    triggerQueueReading();
  }

  @Override
  public boolean isSuspended() {
    return suspended;
  }

  @Override
  public void waitForAllTasksDoneAndDrain() {
    waitInternal(true);
  }

  @Override
  public void waitForAllTasksDone() {
    waitInternal(false);
  }

  public void waitInternal(boolean interrupt) {
    if (queueListener.get() == null) {
      return;
    }

    long MAX_TIMEOUT = 1000;
    long start = System.currentTimeMillis();


    while (queueListener.get().isRunning()) {
      try {
        TimeUnit.MILLISECONDS.sleep(100);

        if (interrupt) {
          queueListener.get().getThread().interrupt();
        } else if (System.currentTimeMillis() - start > MAX_TIMEOUT) {
          log.warn("Waited for {}ms, will now interrupt the thread", MAX_TIMEOUT);
          queueListener.get().getThread().interrupt();
        }
      } catch (InterruptedException e) {
        log.trace("Got Interrupted while waiting for tasks.", e);
      }
    }
  }

  @Override
  public List<Runnable> getSuspendedTasks() {
    return null;
  }

  public Queue<Runnable> getQueue() {
    return queue;
  }

  public int getActiveCount() {
    return queue.size();
  }

  protected static class QueueListener implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(QueueListener.class);
    protected final JavaFXExecutorService service;
    protected final Queue<Runnable> queue;
    protected volatile boolean running = true;
    protected volatile Thread thread;

    public QueueListener(JavaFXExecutorService service) {
      this.service = service;
      queue = service.getQueue();
    }

    @Override
    public void run() {
      log.trace("Start queue processing.");
      thread = Thread.currentThread();
      if (queue.isEmpty()) {
        running = false;
        return;
      }
      if (isCurrentThread()) {
        try {
          executeQueuedRunnables();
        } finally {
          running = false;
        }
      } else {
        log.warn("Not in FX application thread. Will not execute runnables");
      }
    }

    public void executeQueuedRunnables() {
      long millis = System.currentTimeMillis();
      int count = 1;

      Runnable runnable = queue.peek();
      while (runnable != null && shouldResume(count, millis)) {
        log.debug("Executing runnable #{}", count);
        try {
          runnable.run();
        } finally {
          queue.poll();
          count++;
        }
        runnable = queue.peek();
      }
    }

    private boolean isCurrentThread() {
      if (service.mock != null) {
        return true;
      } else {
        return Platform.isFxApplicationThread();
      }
    }

    protected boolean shouldResume(int count, long startTime) {
      return serviceIsRunning();
    }

    private boolean serviceIsRunning() {
      return !service.isShutdown() && !service.isSuspended();
    }

    public void reschedule() {
      Platform.runLater(this);
    }

    public boolean isRunning() {
      return running;
    }

    public Thread getThread() {
      return thread;
    }
  }
}
