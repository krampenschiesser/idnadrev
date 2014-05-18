/*
 * Copyright [${YEAR}] [Christian Loehnert]
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

package de.ks.eventsystem.bus;


import de.ks.LauncherRunner;
import de.ks.reflection.PropertyPath;
import de.ks.reflection.ReflectionUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
@RunWith(LauncherRunner.class)
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
