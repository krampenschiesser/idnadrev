import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Copyright [2014] [Christian Loehnert]
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
}
