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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.ks.activity.context.ActivityScoped;
import de.ks.executor.LoggingUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Vetoed;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

@ActivityScoped
@Vetoed
public class ActivityExecutor implements ScheduledExecutorService {
  private static final Logger log = LoggerFactory.getLogger(ActivityExecutor.class);
  private final String name;
  private final ScheduledThreadPoolExecutor delegate;

  protected ActivityExecutor() {
    this("NeverCalled", 2, 4);//but needed for cdi proxy
  }

  public ActivityExecutor(String name, int corePoolSize, int maximumPoolSize) {
    delegate = new ScheduledThreadPoolExecutor(corePoolSize, new ThreadFactoryBuilder().setDaemon(true).setNameFormat(name + "-%d").setUncaughtExceptionHandler(new LoggingUncaughtExceptionHandler()).build());
    this.name = name;
    delegate.setMaximumPoolSize(maximumPoolSize);
    delegate.setKeepAliveTime(1, TimeUnit.MINUTES);
    delegate.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
  }

  public String getName() {
    return name;
  }

  public void waitForAllTasksDone() {
    while (!delegate.isShutdown() && delegate.getActiveCount() > 0) {
      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException e) {
        log.trace("Got interrupted while waiting for tasks.", e);
      }
    }
  }

  @Override
  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    return delegate.schedule(command, delay, unit);
  }

  @Override
  public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
    return delegate.schedule(callable, delay, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
    return delegate.scheduleAtFixedRate(command, initialDelay, period, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
    return delegate.scheduleWithFixedDelay(command, initialDelay, delay, unit);
  }

  @Override
  public void execute(Runnable command) {
    delegate.execute(command);
  }

  @Override
  public Future<?> submit(Runnable task) {
    return delegate.submit(task);
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    return delegate.submit(task, result);
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    return delegate.submit(task);
  }

  @Override
  public void shutdown() {
    delegate.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    return delegate.shutdownNow();
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
    return delegate.invokeAny(tasks);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return delegate.invokeAny(tasks, timeout, unit);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
    return delegate.invokeAll(tasks);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
    return delegate.invokeAll(tasks, timeout, unit);
  }

  @Override
  public boolean isShutdown() {
    return delegate.isShutdown();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return delegate.awaitTermination(timeout, unit);
  }

  public boolean isTerminating() {
    return delegate.isTerminating();
  }

  @Override
  public boolean isTerminated() {
    return delegate.isTerminated();
  }
}
