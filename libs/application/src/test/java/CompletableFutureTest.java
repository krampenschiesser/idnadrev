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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class CompletableFutureTest {

  private ExecutorService service;

  @Before
  public void setUp() throws Exception {
    service = Executors.newFixedThreadPool(4);
  }

  private static final Logger log = LoggerFactory.getLogger(CompletableFutureTest.class);

  @Ignore
  @Test
  public void testName() throws Exception {
    CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> 42, service);
    CompletableFuture<Integer> other = CompletableFuture.supplyAsync(() -> 1, service);

    CompletableFuture<Void> first = future.thenAcceptBothAsync(other, (integer, integer2) -> {
      try {
        log.info("Start sleeping {}*{}*100", integer, integer2);
        Thread.sleep(integer * integer2 * 100);
        log.info("Done sleeping {}*{}*100", integer, integer2);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }, service);
    CompletableFuture<Void> second = future.thenAcceptBothAsync(other, (integer, integer2) -> {
      try {
        log.info("Start sleeping {}*{}*200", integer, integer2);
        Thread.sleep(integer * integer2 * 200);
        log.info("Done sleeping {}*{}*200", integer, integer2);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }, service);
    CompletableFuture<Void> third = future.thenAcceptBothAsync(other, (integer, integer2) -> {
      try {
        log.info("Start sleeping {}*{}*300", integer, integer2);
        Thread.sleep(integer * integer2 * 300);
        log.info("Done sleeping {}*{}*300", integer, integer2);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }, service);

    CompletableFuture.allOf(third, CompletableFuture.allOf(first, second)).join();
  }

  @Test
  public void testSecondThen() throws Exception {
    AtomicInteger result = new AtomicInteger();
    AtomicInteger cumulation = new AtomicInteger();
    CompletableFuture<Integer> future = new CompletableFuture<>();
    Consumer<Integer> consumer = i -> {
      result.set(i);
      cumulation.addAndGet(i);
    };
    assertEquals(0, future.getNumberOfDependents());

    CompletableFuture<Void> first = future.thenApply(i -> i + 1).thenAccept(consumer);
    CompletableFuture<Void> second = future.thenApply(i -> i + 2).thenAccept(consumer);
    CompletableFuture<Void> third = future.thenApply(i -> i + 3).thenAccept(consumer);
    CompletableFuture<Void> fourth = future.thenApply(i -> i + 4).thenAccept(consumer);
    assertEquals(4, future.getNumberOfDependents());

    assertNotSame(first, second);
    first.exceptionally(t -> null);
    first.thenRun(() -> "".toCharArray());
    assertEquals(2, first.getNumberOfDependents());
    assertEquals(0, second.getNumberOfDependents());

    future.complete(1);
    assertEquals(2, result.intValue());
    assertTrue(first.isDone());
    assertTrue(second.isDone());
    assertTrue(third.isDone());
    assertTrue(fourth.isDone());

    assertEquals(14, cumulation.get());
  }

  @Test
  public void testExceptionally() throws Exception {
    AtomicInteger received = new AtomicInteger();

    CompletableFuture<Integer> future = new CompletableFuture<>();
    future.thenAccept(i -> i++)//
            .thenRun(() -> {
              throw new RuntimeException("test");
            })//
            .thenRun(() -> "".toString())//
            .thenRun(() -> "".toString())//
            .exceptionally(t -> {
              received.incrementAndGet();
              return null;
            });
    future.exceptionally(t -> {
      received.incrementAndGet();
      received.incrementAndGet();
      return 0;
    });

    assertEquals(2, future.getNumberOfDependents());

    future.complete(1);
    assertEquals(1, received.get());
  }
}
