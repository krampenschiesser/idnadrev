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

package de.ks.eventsystem.bus;

import de.ks.LauncherRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 *
 */
@RunWith(LauncherRunner.class)
public class EventBusTest {
  private EventBus eventBus;

  @Before
  public void setUp() throws Exception {
    eventBus = new EventBus();
  }

  @Test
  public void testHandlerRegistering() throws Exception {
    eventBus.register(new Handler());
    List<EventHandler> handlers = eventBus.handlers.get(Object.class);

    assertEquals(3, handlers.size());
    assertEquals("validHandler", handlers.get(0).method.getName());
    assertEquals("validNonConsumingHandler", handlers.get(1).method.getName());
    assertEquals("validConsumingHandler", handlers.get(2).method.getName());
  }

  @Test
  public void testUnregister() throws Exception {
    Handler first = new Handler();
    Handler second = new Handler();

    eventBus.register(first).register(second);
    assertEquals(6, eventBus.handlers.size());

    eventBus.unregister(first);
    assertEquals(3, eventBus.handlers.size());
  }

  @Test
  public void testPostAsyncInteger() throws Exception {
    int COUNT = 40;
    ReceivingHandler handler = new ReceivingHandler(COUNT);
    eventBus.register(handler);

    int sum = 0;
    for (int i = 0; i < COUNT; i++) {
      eventBus.post(i);
      sum += i;
    }
    assertFalse(sum == handler.getSum());
    handler.getLatch().await(15, TimeUnit.SECONDS);
    assertEquals(sum, handler.getSum());
  }

  @Test
  public void testPostHierarchy() throws Exception {
    Parent parent = new Parent();
    Child child = new Child();

    ReceivingHandler handler = new ReceivingHandler(0);
    eventBus.register(handler);

    eventBus.post(parent);
    assertSame(parent, handler.getParent());

    eventBus.post(child);
    assertSame(child, handler.getParent());
    assertSame(child, handler.getChild());
  }
}
