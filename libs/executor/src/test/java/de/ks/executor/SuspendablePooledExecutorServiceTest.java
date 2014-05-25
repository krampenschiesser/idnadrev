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

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SuspendablePooledExecutorServiceTest {

  private SuspendablePooledExecutorService executor;
  private static final LongAdder adder = new LongAdder();

  @Before
  public void setUp() throws Exception {
    executor = new SuspendablePooledExecutorService(2, 2);
    adder.reset();
  }

  @Test
  public void testSuspendResume() throws Exception {
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
    executor.waitForAllTasksDone();
    assertEquals(0, executor.getActiveCount());
    assertEquals(0, executor.getQueue().size());
    assertEquals(3, adder.sum());
  }

  @Test
  public void testCompletionChain() throws Exception {
    CompletableFuture.runAsync(createSleepingRunnable(300), executor)//
            .thenRun(createSleepingRunnable(100))//
            .thenRunAsync(createSleepingRunnable(100), executor);
    assertEquals(1, executor.getActiveCount());


    long start = System.currentTimeMillis();
    executor.suspend();
    long stop = System.currentTimeMillis();
    assertTrue(stop - start >= 400);

    assertEquals(1, executor.getSuspendedTasks().size());
    assertEquals(2, adder.sum());

    executor.resume();
    TimeUnit.MILLISECONDS.sleep(10);
    assertEquals(1, executor.getActiveCount());

    TimeUnit.MILLISECONDS.sleep(100);
    assertEquals(3, adder.sum());
  }

  Runnable createSleepingRunnable(int time) {
    return () -> {
      try {
        TimeUnit.MILLISECONDS.sleep(time);
        adder.increment();
      } catch (InterruptedException e) {
        //
      }
    };
  }
}
