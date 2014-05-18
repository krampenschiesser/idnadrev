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
package de.ks.launch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public abstract class Service {
  private static final Logger log = LoggerFactory.getLogger(Service.class);
  private volatile ServiceRuntimeState state = ServiceRuntimeState.STOPPED;
  private volatile CountDownLatch latch = new CountDownLatch(1);
  protected ExecutorService executorService;

  public Service start() {
    if (state != ServiceRuntimeState.STOPPED) {
      await();
    }
    resetLatch();

    state = ServiceRuntimeState.STARTING;
    try {
      doStart();
      state = ServiceRuntimeState.RUNNING;
    } catch (Throwable t) {
      log.error("Could not start service {}", getName(), t);
      state = ServiceRuntimeState.STOPPED;
      throw t;
    } finally {
      latch.countDown();
    }
    return this;
  }

  private void resetLatch() {
    latch = new CountDownLatch(1);
  }

  public Service stop() {
    if (state != ServiceRuntimeState.RUNNING) {
      await();
    }
    resetLatch();

    state = ServiceRuntimeState.STOPPING;
    try {
      doStop();
      state = ServiceRuntimeState.STOPPED;
    } catch (Exception e) {
      log.error("Could not stop service {}", getName(), e);
      state = ServiceRuntimeState.RUNNING;
      throw e;
    } finally {
      latch.countDown();
    }

    return this;
  }

  public void await() {
    try {
      latch.await();
    } catch (InterruptedException e) {
      log.warn("Got interrupted ", e);
    }
  }

  public ServiceRuntimeState getState() {
    return state;
  }

  //default methods

  public int getPriority() {
    return 1;
  }

  public String getName() {
    return getClass().getSimpleName();
  }

  public boolean isRunning() {
    return getState() == ServiceRuntimeState.RUNNING;
  }

  public boolean isStopped() {
    return getState() == ServiceRuntimeState.STOPPED;
  }

  public void waitUntilRunning() {
    await();
  }

  public void waitUntilStopped() {
    await();
  }

  public void initialize(ExecutorService executorService, String[] args) {
    this.executorService = executorService;
  }

  protected abstract void doStart();

  protected abstract void doStop();

  @Override
  public String toString() {
    return getName();
  }
}
