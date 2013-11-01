package de.ks.eventsystem.bus;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.reflection.PropertyPath;
import de.ks.reflection.ReflectionUtil;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class EventHandlerTest {
  @Test
  public void testEventHandling() throws Exception {
    PropertyPath<Handler> path = PropertyPath.of(Handler.class);
    path.build().validConsumingHandler(null);
    Method consumingMethod = path.getLastMethod();

    path.build().validNonConsumingHandler(null);
    Method nonConsumingMethod = path.getLastMethod();

    Method validMethod = ReflectionUtil.getMethod(Handler.class, "validHandler");

    assertTrue(new EventHandler(new Handler(), consumingMethod).handleEvent(null, false));
    assertFalse(new EventHandler(new Handler(), nonConsumingMethod).handleEvent(null, false));
    assertFalse(new EventHandler(new Handler(), validMethod).handleEvent(null, false));
  }
}
