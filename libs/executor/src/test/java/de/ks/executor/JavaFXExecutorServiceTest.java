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

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class JavaFXExecutorServiceTest {
  private static final Logger log = LoggerFactory.getLogger(JavaFXExecutorServiceTest.class);
  private static final LongAdder adder = new LongAdder();
  private ExecutorService delegate;
  private JavaFXExecutorService executor;

  @Before
  public void setUp() throws Exception {
    delegate = Executors.newSingleThreadExecutor();
    executor = new JavaFXExecutorService(delegate);
    adder.reset();
  }

  @After
  public void tearDown() throws Exception {
    delegate.shutdownNow();
  }

  @Test
  public void testSuspend() throws Exception {
    int total = 10;
    for (int i = 0; i < total; i++) {
      executor.submit(createSleepingRunnable(100));
    }
    Thread.sleep(100);
    executor.suspend();
    int activeCount = executor.getActiveCount();
    assertThat(activeCount, Matchers.lessThan(20));
    Thread.sleep(200);
    assertEquals(activeCount, executor.getActiveCount());
    executor.resume();
    executor.waitForAllTasksDone();
    assertEquals(0, executor.getActiveCount());
    assertEquals(total, adder.sum());
  }

  @Test(timeout = 200)
  public void testLongRunningInterrupt() throws Exception {
    executor.submit(createSleepingRunnable(10000));
    executor.shutdown();
  }

  @Test(timeout = 2000)
  public void testLongRunningTimeout() throws Exception {
    executor.submit(createSleepingRunnable(10000));
    executor.suspend();
  }

  int count;

  Runnable createSleepingRunnable(int time) {
    final int id = ++count;
    return () -> {
      try {
        log.info("Running     {}", id);
        TimeUnit.MILLISECONDS.sleep(time);
        log.info("Done with   {}", id);
        adder.increment();
      } catch (InterruptedException e) {
        log.info("Interrupted {}", id);
      }
    };
  }
}