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

import org.junit.Test;

import java.util.concurrent.*;

import static org.junit.Assert.assertTrue;

public class CancelRejectionTest {
  @Test
  public void testCancelAfterShutdown() throws Exception {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 4, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    executor.setRejectedExecutionHandler(new CancelRejection());
    executor.shutdown();

    CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> 42, executor);
    assertTrue(future.isCancelled());

    FutureTask<Integer> futureTask = new FutureTask<>(() -> 42);
    Future<?> otherFuture = executor.submit(futureTask);
    assertTrue(otherFuture.isCancelled());
  }

  @Test
  public void testCancelAfterShutdownWithScheduledExecutor() throws Exception {
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    executor.setRejectedExecutionHandler(new CancelRejection());
    executor.shutdown();

    CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> 42, executor);
    assertTrue(future.isCancelled());

    FutureTask<Integer> futureTask = new FutureTask<>(() -> 42);
    executor.submit(futureTask);
    assertTrue(futureTask.isCancelled());
  }

}