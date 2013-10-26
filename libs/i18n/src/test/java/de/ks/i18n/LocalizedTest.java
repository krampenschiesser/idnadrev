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
    String helloWorld = Localized.get("/hello");
    assertEquals("Hello world!", helloWorld);

    Localized.changeLocale(Locale.GERMAN);
    helloWorld = Localized.get("/hello");
    assertEquals("Hallo Welt!", helloWorld);


    Localized.changeLocale(Locale.ENGLISH);
    helloWorld = Localized.get("/hello");
    assertEquals("Hello world!", helloWorld);
  }

  @Test
  public void testParameters() throws Exception {
    String helloSauerland = Localized.get("/hello/parametererized", "Sauerland", "!!!");
    assertEquals("Hello Sauerland!!!", helloSauerland);
  }

  @Test
  public void testParametersPositioned() throws Exception {
    String helloSauerland = Localized.get("/hello/positioned", "!!!", "Sauerland");
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
