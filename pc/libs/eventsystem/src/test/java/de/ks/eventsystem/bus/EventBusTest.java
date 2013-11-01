package de.ks.eventsystem.bus;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

import static org.junit.Assert.*;

/**
 *
 */
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
    ReceivingHandler handler = new ReceivingHandler();
    eventBus.register(handler);

    int sum = 0;
    for (int i = 0; i < 40; i++) {
      eventBus.post(i);
      sum += i;
    }
    assertFalse(sum == handler.getSum());
    while (ForkJoinPool.commonPool().getQueuedSubmissionCount() > 0) {
      Thread.sleep(10);
    }
    assertEquals(sum, handler.getSum());
  }

  @Test
  public void testPostHierarchy() throws Exception {
    Parent parent = new Parent();
    Child child = new Child();

    ReceivingHandler handler = new ReceivingHandler();
    eventBus.register(handler);

    eventBus.post(parent);
    assertSame(parent, handler.getParent());

    eventBus.post(child);
    assertSame(child, handler.getParent());
    assertSame(child, handler.getChild());
  }
}
