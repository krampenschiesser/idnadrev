/*
 * Copyright [${YEAR}] [Christian Loehnert]
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


import de.ks.LauncherRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class ExecutorServiceTest {
  private static final Logger log = LoggerFactory.getLogger(ExecutorServiceTest.class);

  ThreadLocal<String> threadLocal = new ThreadLocal<>();
  protected volatile String value;

  @Test
  public void testThreadPropagation() throws Exception {
    MyThreadCallBoundValue.threadLocal = threadLocal;

    threadLocal.set("Hello Sauerland!");
    for (int i = 0; i < 50; i++) {
      Future<?> future = ExecutorService.instance.submit((Runnable) () -> {
        log.info("Receiving {}", threadLocal.get());
        value = threadLocal.get();
      });
      future.get();
    }
    assertEquals("Hello Sauerland!", value);
  }

}
