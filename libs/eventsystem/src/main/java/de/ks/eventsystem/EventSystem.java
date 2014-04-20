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

package de.ks.eventsystem;


import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import de.ks.eventsystem.bus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Produces;

public class EventSystem {
  private static final Logger log = LoggerFactory.getLogger(EventSystem.class);

  private static final EventBus bus;

  public static void setWaitForEvents(boolean wait) {
    EventBus.alwaysWait = wait;
  }

  static {
    bus = new EventBus();
    bus.register(new EventSystem());
  }

  @Subscribe
  public void onDeadEvent(DeadEvent dead) {
    log.warn("No handler for event \"{}\" found. Contents: {}", dead.getEvent().getClass().getSimpleName(), dead.getEvent());
  }


  @Produces
  public EventBus getEventBus() {
    return bus;
  }
}
