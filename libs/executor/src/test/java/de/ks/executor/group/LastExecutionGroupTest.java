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

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.Assert.*;

public class LastExecutionGroupTest {

  private ExecutorService executorService;
  private LastExecutionGroup<Integer> lastExecutionGroup;

  @Before
  public void setUp() throws Exception {
    executorService = Executors.newFixedThreadPool(2);
    lastExecutionGroup = new LastExecutionGroup<Integer>("test", 50, executorService);
    warmUpPool();
  }

  @After
  public void tearDown() throws Exception {
    executorService.shutdown();
    executorService.awaitTermination(5, TimeUnit.SECONDS);
  }

  @Test
  public void testMultipleEvents() throws Exception {
    int total = 30;

    LongAdder adder = new LongAdder();
    CompletableFuture<Integer> future = null;

    long startTime = System.currentTimeMillis();
    for (int i = 0; i < total; i++) {
      final int number = i;
      future = lastExecutionGroup.schedule(() -> this.sleep(number + 1, 100, adder));
    }
    assertNotNull(future);
    Integer result = future.get();
    long stopTime = System.currentTimeMillis();
    assertNotNull(result);


    assertEquals(total, result.intValue());

    assertEquals(1, adder.sum());
    assertThat(stopTime - startTime, Matchers.lessThan(200L));
  }

  @Test
  public void testEventLater() throws Exception {
    LongAdder adder = new LongAdder();

    lastExecutionGroup.schedule(() -> this.sleep(1, 100, adder));
    Thread.sleep(110);
    CompletableFuture<Integer> future = lastExecutionGroup.schedule(() -> this.sleep(2, 100, adder));

    assertNotNull(future);
    Integer result = future.get();
    assertNotNull(result);

    assertEquals(2, adder.sum());
  }

  @Test
  public void testWaitTime() throws Exception {
    long startTime = System.currentTimeMillis();
    CompletableFuture<Integer> future = lastExecutionGroup.schedule(() -> this.sleep(0, 0, null));
    long stopTime = System.currentTimeMillis();
    assertThat(stopTime - startTime, Matchers.lessThan(55L));
  }

  protected void warmUpPool() throws InterruptedException {
    List<Callable<Integer>> tasks = Arrays.asList(createSleepingCallable(10), createSleepingCallable(10), createSleepingCallable(10), createSleepingCallable(10));
    executorService.invokeAll(tasks);
  }

  public Callable<Integer> createSleepingCallable(int timeout) {
    return () -> sleep(0, 10, null);
  }

  public int sleep(int number, int timeout, LongAdder adder) {
    if (timeout > 0) {
      try {
        Thread.sleep(timeout);
      } catch (InterruptedException e) {
        //
      }
    }
    if (adder != null) {
      adder.increment();
    }
    return number;
  }
}