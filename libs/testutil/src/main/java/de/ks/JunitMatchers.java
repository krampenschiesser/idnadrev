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

package de.ks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 *
 */
public class JunitMatchers {
  private static final Logger log = LoggerFactory.getLogger(JunitMatchers.class);

  public static boolean withRetry(Callable<Boolean> delegate) {
    int rate = 20;
    int timeout = 5000;
    int count = 0;

    boolean success = false;
    while (count < timeout / 20) {
      try {
        if (delegate.call()) {
          return true;
        }
        Thread.sleep(rate);
        count++;
      } catch (Exception e) {
        log.error("Could not execute {}", delegate, e);
        return false;
      }
    }
    return false;
  }
}