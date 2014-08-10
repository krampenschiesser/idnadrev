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
package de.ks.activity.executor;

import de.ks.activity.context.ActivityContext;
import de.ks.activity.context.ActivityScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import java.util.concurrent.TimeUnit;

public class ActivityExecutorProducer {
  private static final Logger log = LoggerFactory.getLogger(ActivityExecutorProducer.class);

  @Produces
  @ActivityScoped
  public ActivityExecutor createExecutorService(ActivityContext context) {
    return new ActivityExecutor(context.getCurrentActivity(), 2, Runtime.getRuntime().availableProcessors());
  }

  public void shutdownActivityExecutor(@Disposes ActivityExecutor executor) {
    if (!executor.isShutdown()) {
      log.debug("Shutting down executor {}", executor.getName());
      executor.shutdownNow();
      try {
        executor.awaitTermination(5, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        //
      }
    }
  }
}
