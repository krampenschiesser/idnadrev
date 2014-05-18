/**
 * Copyright [2014] [Christian Loehnert]
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Alternative
public class MyThreadCallBoundValue implements ThreadCallBoundValue {
  private static final Logger log = LoggerFactory.getLogger(MyThreadCallBoundValue.class);
  String currentValue = null;
  static ThreadLocal<String> threadLocal = new ThreadLocal<>();

  @Override
  public void initializeInCallerThread() {
    log.trace("initializing");
    currentValue = threadLocal.get();
  }

  @Override
  public void doBeforeCallInTargetThread() {
    if (threadLocal.get() != null) {
      throw new RuntimeException("ThreadLocal not set to null again!");
    }
    log.trace("setting value");
    threadLocal.set(currentValue);
  }

  @Override
  public void doAfterCallInTargetThread() {
    log.trace("reset");
    threadLocal.set(null);
  }

  @Override
  public void registerAgain() {

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
}
