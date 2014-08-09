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
package de.ks.javafx.event;

import javafx.event.Event;
import javafx.event.EventHandler;

import java.util.Arrays;
import java.util.List;

public class ChainedEventHandler<T extends Event> implements EventHandler<T> {
  protected final List<EventHandler<T>> chain;

  public ChainedEventHandler(EventHandler<T> first, EventHandler<T> second) {
    chain = Arrays.asList(first, second);
  }

  public ChainedEventHandler(EventHandler<T>... handlers) {
    chain = Arrays.asList(handlers);
  }

  @Override
  public void handle(T event) {
    chain.forEach(h -> {
      if (!event.isConsumed()) {
        h.handle(event);
      }
    });
  }
}
