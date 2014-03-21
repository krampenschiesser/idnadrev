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

package de.ks.executor;


import com.google.common.util.concurrent.*;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Vetoed;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * The thread pool for the application.
 * It delegates to the {@link ScheduledExecutorService}.
 * <p>
 * But there is one major difference: {@link #getPropagations()}
 * It will wrap each task that is submitted to the thread pool in order to execute all registered
 * {@link ThreadCallBoundValue}.
 */
@Vetoed
public class ExecutorService implements ListeningScheduledExecutorService {
  private static final Logger log = LoggerFactory.getLogger(ExecutorService.class);
  public static final ExecutorService instance = new ExecutorService();

  protected final ListeningScheduledExecutorService pool;
  protected final ThreadPropagations propagations;
  private final ScheduledThreadPoolExecutor delegate;

  private ExecutorService() {
    delegate = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), new SimpleThreadFactory());
    pool = MoreExecutors.listeningDecorator(delegate);
    propagations = new ThreadPropagations();
  }

  public ThreadPropagations getPropagations() {
    return propagations;
  }

  @Override
  public ListenableScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    command = wrap(command);
    return pool.schedule(command, delay, unit);
  }

  @Override
  public <V> ListenableScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
    callable = wrap(callable);
    return pool.schedule(callable, delay, unit);
  }

  @Override
  public ListenableScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
    command = wrap(command);
    return pool.scheduleAtFixedRate(command, initialDelay, period, unit);
  }

  @Override
  public ListenableScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
    command = wrap(command);
    return pool.scheduleWithFixedDelay(command, initialDelay, delay, unit);
  }

  @Override
  public void shutdown() {
    pool.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    return pool.shutdownNow();
  }

  @Override
  public boolean isShutdown() {
    return pool.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return pool.isTerminated();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return pool.awaitTermination(timeout, unit);
  }

  @Override
  public <T> ListenableFuture<T> submit(Callable<T> task) {
    task = wrap(task);
    return pool.submit(task);
  }

  @Override
  public <T> ListenableFuture<T> submit(Runnable task, T result) {
    task = wrap(task);
    return pool.submit(task, result);
  }

  @Override
  public ListenableFuture<?> submit(Runnable task) {
    task = wrap(task);
    return pool.submit(task);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
    tasks = wrap(tasks);
    return pool.invokeAll(tasks);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
    tasks = wrap(tasks);
    return pool.invokeAll(tasks, timeout, unit);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
    tasks = wrap(tasks);
    return pool.invokeAny(tasks);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    tasks = wrap(tasks);
    return pool.invokeAny(tasks, timeout, unit);
  }

  @Override
  public void execute(Runnable command) {
    command = wrap(command);
    pool.execute(command);
  }

  public void invokeInJavaFXThread(Runnable command) {
    final Runnable runner = wrap(command);

    @SuppressWarnings("unchecked") FutureTask futureTask = new FutureTask(() -> {
      runner.run();
      return null;
    });
    executeInJavaFXThreadInternal(futureTask);
    try {
      futureTask.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  public void executeInJavaFXThread(Runnable command) {
    command = wrap(command);
    executeInJavaFXThreadInternal(command);
  }

  public <V> ListenableFuture<V> executeInJavaFXThread(Callable<V> command) {
    final Callable<V> runner = wrap(command);
    ListenableFutureTask<V> futureTask = ListenableFutureTask.create(runner);
    executeInJavaFXThreadInternal(futureTask);
    return futureTask;
  }

  public <V> V loadInJavaFXThread(Callable<V> command) {
    final Callable<V> runner = wrap(command);
    ListenableFutureTask<V> futureTask = ListenableFutureTask.create(runner);
    executeInJavaFXThreadInternal(futureTask);
    try {
      return futureTask.get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Could not invoke " + command, e);
      throw new RuntimeException(e);
    }
  }

  protected void executeInJavaFXThreadInternal(Runnable runnable) {
    if (Platform.isFxApplicationThread()) {
      runnable.run();
    } else {
      Platform.runLater(runnable);
    }
  }

  public void invokeAndWait(Runnable runnable) {
    Future<?> submit = submit(runnable);
    try {
      submit.get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Could not execute " + runnable, e);
      throw new RuntimeException(e);
    }
  }

  protected <T> Collection<? extends Callable<T>> wrap(Collection<? extends Callable<T>> tasks) {
    ArrayList<Callable<T>> retval = new ArrayList<>(tasks.size());
    for (Callable<T> task : tasks) {
      retval.add(wrap(task));
    }
    return retval;
  }

  protected <T> Callable<T> wrap(final Callable<T> delegate) {
    final Collection<ThreadCallBoundValue> threadCallBoundValues = propagations.getPropagations();
    for (ThreadCallBoundValue threadCallBoundValue : threadCallBoundValues) {
      threadCallBoundValue.initializeInCallerThread();
    }
    Callable<T> runnable = new Callable<T>() {
      @Override
      public T call() throws Exception {
        for (ThreadCallBoundValue threadCallBoundValue : threadCallBoundValues) {
          threadCallBoundValue.doBeforeCallInTargetThread();
        }
        try {
          return delegate.call();
        } catch (Throwable t) {
          log.error("Could not execute async action: ", t);
          throw t;
        } finally {
          for (ThreadCallBoundValue threadCallBoundValue : threadCallBoundValues) {
            threadCallBoundValue.doAfterCallInTargetThread();
          }
        }
      }
    };
    return runnable;

  }

  protected Runnable wrap(final Runnable delegate) {
    final Collection<ThreadCallBoundValue> threadCallBoundValues = propagations.getPropagations();
    for (ThreadCallBoundValue threadCallBoundValue : threadCallBoundValues) {
      threadCallBoundValue.initializeInCallerThread();
    }
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        for (ThreadCallBoundValue threadCallBoundValue : threadCallBoundValues) {
          threadCallBoundValue.doBeforeCallInTargetThread();
        }
        try {
          delegate.run();
        } finally {
          for (ThreadCallBoundValue threadCallBoundValue : threadCallBoundValues) {
            threadCallBoundValue.doAfterCallInTargetThread();
          }
        }
      }
    };
    return runnable;
  }

  public BlockingQueue<Runnable> getQueue() {
    return delegate.getQueue();
  }
}
