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
package de.ks.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.OptimisticLockException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class Retry {
  private static final Logger log = LoggerFactory.getLogger(Retry.class);

  int maxRetries = 10;
  int currentRetries = 0;

  public void retry(Runnable runnable) {
    retry(() -> {
      runnable.run();
      return null;
    });
  }

  public <T> T retry(Supplier<T> runnable) {
    Throwable lastException = null;

    while (shouldRetry(lastException)) {
      try {
        waitBecauseOfRetry();
        return runnable.get();
      } catch (Throwable t) {
        lastException = t;
        currentRetries++;
      }
    }
    if (lastException != null) {
      log.error("could not execute runnable, failed after {} retries. ", currentRetries, lastException);
      throw new RuntimeException(lastException);
    }
    return null;
  }

  private void waitBecauseOfRetry() {
    if (currentRetries > 0) {
      double sleepTime = Math.exp(currentRetries);
      double nextDouble = ThreadLocalRandom.current().nextDouble(sleepTime, sleepTime + 3);
      try {
        Thread.sleep((long) sleepTime);
      } catch (InterruptedException e) {
        //nope
      }
    }
  }

  protected boolean shouldRetry(Throwable lastException) {
    if (currentRetries == maxRetries) {
      return false;
    }
    boolean retry = lastException == null;
    retry = retry || lastException instanceof OptimisticLockException;
    return retry;
  }
}
