/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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
package de.ks.executor.fx;

import de.ks.executor.ThreadCallBoundValue;
import javafx.event.Event;
import javafx.event.EventHandler;

import javax.enterprise.inject.spi.CDI;
import java.util.HashSet;
import java.util.Set;


public class ContextualEventHandler<T extends Event> implements EventHandler<T> {
  private final Set<ThreadCallBoundValue> threadCallBoundValues = new HashSet<>();
  private final EventHandler<T> delegate;

  public ContextualEventHandler(EventHandler<T> delegate) {
    this.delegate = delegate;
    for (ThreadCallBoundValue threadCallBoundValue : CDI.current().select(ThreadCallBoundValue.class)) {
      threadCallBoundValues.add(threadCallBoundValue);
    }

    threadCallBoundValues.forEach(ThreadCallBoundValue::initializeInCallerThread);
  }

  @Override
  public void handle(T event) {
    threadCallBoundValues.forEach(ThreadCallBoundValue::doBeforeCallInTargetThread);
    try {
      delegate.handle(event);
    } finally {
      threadCallBoundValues.forEach(ThreadCallBoundValue::doAfterCallInTargetThread);
    }
  }
}
