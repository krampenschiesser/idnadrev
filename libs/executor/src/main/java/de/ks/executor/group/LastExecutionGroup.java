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
package de.ks.executor.group;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class LastExecutionGroup<T> implements Runnable {
  static class LastExecutionEvent<T> {
    protected final long scheduleTime;
    private final Supplier<T> delegate;

    public LastExecutionEvent(Supplier<T> delegate) {
      this.delegate = delegate;
      this.scheduleTime = System.currentTimeMillis();
    }

    public long getScheduleTime() {
      return scheduleTime;
    }

    public Supplier<T> getDelegate() {
      return delegate;
    }
  }

  private static final Logger log = LoggerFactory.getLogger(LastExecutionGroup.class);

  protected final LinkedBlockingDeque<LastExecutionEvent<T>> queue = new LinkedBlockingDeque<>();
  private final String desc;
  protected final long waitTime;
  protected ExecutorService executor;
  protected final AtomicBoolean running = new AtomicBoolean(false);

  protected volatile CompletableFuture<T> result;

  public LastExecutionGroup(String desc, long waitTime, ExecutorService executor) {
    this.desc = desc;
    this.waitTime = waitTime;
    this.executor = executor;
  }

  public CompletableFuture<T> schedule(Supplier<T> supplier) {
    queue.add(new LastExecutionEvent<T>(supplier));
    if (running.compareAndSet(false, true)) {
      result = new CompletableFuture<>();
      executor.submit(this);
    }
    return result;
  }

  public void setExecutor(ExecutorService executor) {
    this.executor = executor;
  }

  public void run() {
    log.info("Starting execution group {}.", desc);
    try {
      T value = null;
      while (!queue.isEmpty()) {
        T nextValue = getValue();
        if (nextValue != null) {
          value = nextValue;
        }
      }
      if (log.isDebugEnabled()) {
        String strVal = String.valueOf(value);
        if (strVal.length() > 100) {
          strVal = strVal.substring(0, 100);
        }
        log.debug("Finished execution group {} with value {}", desc, strVal);
      }
      result.complete(value);
    } catch (Throwable t) {
      log.error("Error while running execution group {}", this, t);
      result.completeExceptionally(t);
    } finally {
      running.set(false);
    }
  }

  protected T getValue() {
    LastExecutionEvent<T> lastEvent = null;
    while (true) {
      try {
        LastExecutionEvent<T> poll = queue.poll(waitTime, TimeUnit.MILLISECONDS);
        if (poll == null) {
          if (lastEvent != null) {
            T value = lastEvent.getDelegate().get();
            return value;
          } else {
            return null;
          }
        } else {
          lastEvent = poll;
        }
      } catch (InterruptedException e) {
        if (lastEvent != null) {
          return lastEvent.getDelegate().get();
        } else {
          return null;
        }
      }
    }
  }

  @Override
  public String toString() {
    return "LastExecutionGroup{" +
            "desc='" + desc + '\'' +
            ", waitTime=" + waitTime +
            '}';
  }
}
