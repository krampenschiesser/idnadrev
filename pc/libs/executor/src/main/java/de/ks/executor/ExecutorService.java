package de.ks.executor;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

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
 * <p/>
 * But there is one major difference: {@link #getPropagations()}
 * It will wrap each task that is submitted to the thread pool in order to execute all registered
 * {@link ThreadCallBoundValue}.
 */
@Vetoed
public class ExecutorService implements ScheduledExecutorService {
  private static final Logger log = LoggerFactory.getLogger(ExecutorService.class);
  public static final ExecutorService instance = new ExecutorService();

  protected final ScheduledExecutorService pool;
  protected final ThreadPropagations propagations;

  private ExecutorService() {
    pool = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), new SimpleThreadFactory());
    propagations = new ThreadPropagations();
  }

  public ThreadPropagations getPropagations() {
    return propagations;
  }

  @Override
  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    command = wrap(command);
    return pool.schedule(command, delay, unit);
  }

  @Override
  public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
    callable = wrap(callable);
    return pool.schedule(callable, delay, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
    command = wrap(command);
    return pool.scheduleAtFixedRate(command, initialDelay, period, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
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
  public <T> Future<T> submit(Callable<T> task) {
    task = wrap(task);
    return pool.submit(task);
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    task = wrap(task);
    return pool.submit(task, result);
  }

  @Override
  public Future<?> submit(Runnable task) {
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

    FutureTask futureTask = new FutureTask(() -> {
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

  public <V> Future<V> executeInJavaFXThread(Callable<V> command) {
    final Callable<V> runner = wrap(command);
    FutureTask<V> futureTask = new FutureTask<>(runner);
    executeInJavaFXThreadInternal(futureTask);
    return futureTask;
  }

  public <V> V loadInJavaFXThread(Callable<V> command) {
    final Callable<V> runner = wrap(command);
    FutureTask<V> futureTask = new FutureTask<>(runner);
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
}
