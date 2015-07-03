/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks;

import org.hamcrest.Matcher;
import org.junit.Assert;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class Condition {

  public static void waitFor1s(String message, Supplier<Boolean> condition) {
    waitFor(message, condition, 1, TimeUnit.SECONDS, 10, TimeUnit.MILLISECONDS);
  }

  public static <T> void waitFor1s(Supplier<T> actual, Matcher<? super T> matcher) {
    waitFor(null, actual, matcher, 1, TimeUnit.SECONDS, 10, TimeUnit.MILLISECONDS);
  }

  public static <T> void waitFor1s(String message, Supplier<T> actual, Matcher<? super T> matcher) {
    waitFor(message, actual, matcher, 1, TimeUnit.SECONDS, 10, TimeUnit.MILLISECONDS);
  }

  public static void waitFor5s(String message, Supplier<Boolean> condition) {
    waitFor(message, condition, 5, TimeUnit.SECONDS, 100, TimeUnit.MILLISECONDS);
  }

  public static <T> void waitFor5s(Supplier<T> actual, Matcher<? super T> matcher) {
    waitFor(null, actual, matcher, 5, TimeUnit.SECONDS, 100, TimeUnit.MILLISECONDS);
  }

  public static <T> void waitFor5s(String message, Supplier<T> actual, Matcher<? super T> matcher) {
    waitFor(message, actual, matcher, 5, TimeUnit.SECONDS, 100, TimeUnit.MILLISECONDS);
  }

  public static <T> void waitFor(String message, Supplier<T> actual, Matcher<? super T> matcher, int timeout, TimeUnit waitUnit, int pollRate, TimeUnit pollUnit) {
    long start = System.currentTimeMillis();
    long end = start + waitUnit.toMillis(timeout);

    while (!matcher.matches(actual.get()) && System.currentTimeMillis() < end) {
      try {
        Thread.sleep(pollUnit.toMillis(pollRate));
      } catch (InterruptedException e) {
        //
      }
    }
    if (message == null) {
      Assert.assertThat(actual.get(), matcher);
    } else {
      Assert.assertThat(message, actual.get(), matcher);
    }
  }

  public static void waitFor(String message, Supplier<Boolean> condition, int timeout, TimeUnit waitUnit, int pollRate, TimeUnit pollUnit) {
    long start = System.currentTimeMillis();
    long end = start + waitUnit.toMillis(timeout);

    while (!condition.get() && System.currentTimeMillis() < end) {
      try {
        Thread.sleep(pollUnit.toMillis(pollRate));
      } catch (InterruptedException e) {
        //
      }
    }
    if (!condition.get()) {
      Assert.fail(message);
    }
  }
}
