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
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.Assert.*;

public class SuspendablePooledExecutorServiceTest {
  private static final Logger log = LoggerFactory.getLogger(SuspendablePooledExecutorServiceTest.class);
  private SuspendablePooledExecutorService executor;
  private static final LongAdder adder = new LongAdder();
  int count;

  @Before
  public void setUp() throws Exception {
    executor = new SuspendablePooledExecutorService("test", 2, 2);
    adder.reset();
  }

  @Test
  public void testSuspendResume() throws Exception {
    log.info("testSuspendResume");
    executor.submit(createSleepingRunnable(300));
    executor.submit(createSleepingRunnable(300));
    executor.submit(createSleepingRunnable(300));
    assertEquals(2, executor.getActiveCount());

    long start = System.currentTimeMillis();
    executor.suspend();
    long stop = System.currentTimeMillis();
    assertTrue(stop - start > 300);

    assertEquals(1, executor.getSuspendedTasks().size());
    assertEquals(2, adder.sum());
    executor.resume();
    TimeUnit.MILLISECONDS.sleep(100);
    assertEquals(1, executor.getActiveCount());
    executor.waitForAllTasksDoneAndDrain();
    assertEquals(0, executor.getActiveCount());
    assertEquals(0, executor.getQueue().size());
    assertEquals(3, adder.sum());
  }

  @Test
  public void testCompletionChain() throws Exception {
    executor.invokeAll(Arrays.<Callable<String>>asList(() -> "", () -> "", () -> "", () -> ""));

    log.info("testCompletionChain");
    Runnable first = createSleepingRunnable(300);
    Runnable second = createSleepingRunnable(100);
    Runnable third = createSleepingRunnable(100);
    CompletableFuture.runAsync(first, executor)//
            .thenRun(second)//
            .thenRunAsync(third, executor);
    Thread.sleep(10);
    assertEquals(1, executor.getActiveCount());


    long start = System.currentTimeMillis();
    executor.suspend();
    long stop = System.currentTimeMillis();
    assertThat(stop - start, Matchers.greaterThan(400L));

    assertEquals(1, executor.getSuspendedTasks().size());
    assertEquals(2, adder.sum());

    executor.resume();
    TimeUnit.MILLISECONDS.sleep(10);
    assertEquals(1, executor.getActiveCount());

    TimeUnit.MILLISECONDS.sleep(100);
    assertEquals(3, adder.sum());
  }

  @Test
  public void testShutdown() throws Exception {
    log.info("testShutdown");
    Runnable first = createSleepingRunnable(300);
    Runnable second = createSleepingRunnable(100);
    Runnable third = createSleepingRunnable(100);
    CompletableFuture.runAsync(first, executor)//
            .thenRun(second)//
            .thenRunAsync(third, executor);
    assertEquals(1, executor.getActiveCount());

    executor.shutdownNow();

    executor.awaitTermination(1, TimeUnit.SECONDS);
    assertEquals(1, adder.sum());
    // actually the first runnable got interrupted,
    // and although the thread is interrupted the second runnable is successfully executed,
    // althought the executor is shutting down
    // hope this behaviour is changing in the future? seems like a bug.
    // so actually it should be either
    // assertEquals(0, adder.sum()); or
    // assertEquals(2, adder.sum());
  }

  @Test(timeout = 1000)
  public void testScheduledDraining() throws Exception {
    executor.scheduleAtFixedRate(createSleepingRunnable(100), 0, 200, TimeUnit.MILLISECONDS);
    executor.suspend();
  }

  Runnable createSleepingRunnable(int time) {
    final int id = ++count;
    return () -> {
      try {
        log.info("Running     {}", id);
        TimeUnit.MILLISECONDS.sleep(time);
        adder.increment();
        log.info("Done with   {}", id);
      } catch (InterruptedException e) {
        log.info("Interrupted {}", id);
        //
      }
    };
  }
}
