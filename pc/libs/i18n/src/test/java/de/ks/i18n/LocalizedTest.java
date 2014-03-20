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

package de.ks.i18n;

import com.google.common.eventbus.Subscribe;
import de.ks.eventsystem.EventSystem;
import de.ks.i18n.event.LanguageChangedEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */
public class LocalizedTest {
  private LanguageChangedEvent event;

  @Before
  public void setUp() throws Exception {
    Localized.changeLocale(Locale.ENGLISH);
  }

  @Test
  public void testLanguageChange() throws Exception {
    String helloWorld = Localized.get("hello");
    assertEquals("Hello world!", helloWorld);

    Localized.changeLocale(Locale.GERMAN);
    helloWorld = Localized.get("hello");
    assertEquals("Hallo Welt!", helloWorld);

    Localized.changeLocale(Locale.ENGLISH);
    helloWorld = Localized.get("hello");
    assertEquals("Hello world!", helloWorld);
  }

  @Test
  public void testParameters() throws Exception {
    String helloSauerland = Localized.get("hello.parametererized", "Sauerland", "!!!");
    assertEquals("Hello Sauerland!!!", helloSauerland);
  }

  @Test
  public void testParametersPositioned() throws Exception {
    String helloSauerland = Localized.get("hello.positioned", "!!!", "Sauerland");
    assertEquals("Hello Sauerland!!!", helloSauerland);
  }

  @Test
  public void testLanguageChangeEvent() throws Exception {
    EventSystem.bus.register(this);
    try {
      Localized.changeLocale(Locale.GERMAN);
      assertNotNull(event);
      assertEquals(Locale.ENGLISH, event.getOldLocale());
      assertEquals(Locale.GERMAN, event.getNewLocale());
    } finally {
      EventSystem.bus.unregister(this);
    }
  }

  @Subscribe
  public void onLanguageChange(LanguageChangedEvent event) {
    this.event = event;
  }
}
