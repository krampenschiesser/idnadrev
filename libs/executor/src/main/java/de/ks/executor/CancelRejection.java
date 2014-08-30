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

import de.ks.reflection.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class CancelRejection implements RejectedExecutionHandler {
  private static final Logger log = LoggerFactory.getLogger(CancelRejection.class);

  @Override
  public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
    if (r instanceof RunnableScheduledFuture) {
      Object callable = ReflectionUtil.getFieldValue(r, "callable");
      Object cfAsync = ReflectionUtil.getFieldValue(callable, "task");
      r = (Runnable) cfAsync;
    }

    if (r instanceof ForkJoinTask && r.toString().contains("CompletableFuture$Async")) {
      Object dst = ReflectionUtil.getFieldValue(r, "dst");
      if (dst instanceof CompletableFuture) {
        ((CompletableFuture) dst).cancel(false);
        log.debug("Canceled completable future {}", r);
      }
    } else if (r instanceof Future) {
      ((Future) r).cancel(false);
      log.debug("Canceled future {}", r);
    }
  }
}
