package de.ks.executor;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 *
 */
public class ExecutorService implements ScheduledExecutorService {
  public static final MultipleExecutorService instance = new MultipleExecutorService();

  protected final ScheduledExecutorService pool = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

  private MultipleExecutorService() {

  }

  @Override
  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    return pool.schedule(command, delay, unit);
  }

  @Override
  public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
    return pool.schedule(callable, delay, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
    return pool.scheduleAtFixedRate(command, initialDelay, period, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
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
    return pool.submit(task);
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    return pool.submit(task, result);
  }

  @Override
  public Future<?> submit(Runnable task) {
    return pool.submit(task);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
    return pool.invokeAll(tasks);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
    return pool.invokeAll(tasks, timeout, unit);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
    return pool.invokeAny(tasks);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return pool.invokeAny(tasks, timeout, unit);
  }

  @Override
  public void execute(Runnable command) {
    pool.execute(command);
  }
}
