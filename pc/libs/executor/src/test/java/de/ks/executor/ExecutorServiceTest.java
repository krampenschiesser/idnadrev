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
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class ExecutorServiceTest {
  private static final Logger log = LoggerFactory.getLogger(ExecutorServiceTest.class);

  ThreadLocal<String> threadLocal = new ThreadLocal<>();
  protected String value;

  @Test
  public void testThreadPropagation() throws Exception {
    ThreadCallBoundValue threadCallBoundValue = new ThreadCallBoundValue() {
      String currentValue = null;

      @Override
      public void initializeInCallerThread() {
        log.debug("initializing");
        currentValue = threadLocal.get();
      }

      @Override
      public void doBeforeCallInTargetThread() {
        if (threadLocal.get() != null) {
          throw new RuntimeException("ThreadLocal not set to null again!");
        }
        log.debug("setting value");
        threadLocal.set(currentValue);
      }

      @Override
      public void doAfterCallInTargetThread() {
        log.debug("reset");
        threadLocal.set(null);
      }

      @Override
      public ThreadCallBoundValue clone() {
        try {
          ThreadCallBoundValue clone = (ThreadCallBoundValue) super.clone();
          return clone;
        } catch (CloneNotSupportedException e) {
          throw new InternalError("could not clone");
        }
      }
    };
    ExecutorService.instance.getPropagations().register(threadCallBoundValue);

    threadLocal.set("Hello Sauerland!");
    for (int i = 0; i < 50; i++) {
      Future<?> future = ExecutorService.instance.submit((Runnable) () -> value = threadLocal.get());
      future.get();
    }
    assertEquals("Hello Sauerland!", value);
  }
}
