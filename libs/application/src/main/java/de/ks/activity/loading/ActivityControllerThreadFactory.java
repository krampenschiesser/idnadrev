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
package de.ks.activity.loading;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ThreadFactory;

public class ActivityControllerThreadFactory implements ThreadFactory {
  public static boolean isInLoadingThread() {
    return inLoadingThread.get();
  }

  private static ThreadLocal<Boolean> inLoadingThread = ThreadLocal.withInitial(() -> false);
  protected final ThreadFactory delegate = new ThreadFactoryBuilder().setDaemon(true).setNameFormat(getClass().getSimpleName() + "%d").build();

  @Override
  public Thread newThread(Runnable r) {
    return delegate.newThread(() -> {
      inLoadingThread.set(true);
      r.run();
    });
  }
}
