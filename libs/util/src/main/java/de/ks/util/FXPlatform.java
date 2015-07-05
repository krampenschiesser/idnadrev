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
package de.ks.util;

import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class FXPlatform {
  public static void invokeLater(Runnable runnable) {
    if (Platform.isFxApplicationThread()) {
      runnable.run();
    } else {
      CountDownLatch latch = new CountDownLatch(1);
      Platform.runLater(() -> {
        try {
          runnable.run();
        } finally {
          latch.countDown();
        }
      });
      try {
        latch.await();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static <T> T invokeLater(Supplier<T> runnable) {
    if (Platform.isFxApplicationThread()) {
      return runnable.get();
    } else {
      AtomicReference<T> ref = new AtomicReference<>();
      CountDownLatch latch = new CountDownLatch(1);
      Platform.runLater(() -> {
        try {
          ref.set(runnable.get());
        } finally {
          latch.countDown();
        }
      });
      try {
        latch.await();
        return ref.get();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static void waitForFX() {
    if (!Platform.isFxApplicationThread()) {
      invokeLater(new Runnable() {
        @Override
        public void run() {
          //noop
        }
      });
    }
  }
}
