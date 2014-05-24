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

package de.ks.i18n;


import de.ks.eventsystem.bus.EventBus;
import de.ks.i18n.event.LanguageChangedEvent;

import javax.enterprise.inject.spi.CDI;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main interface to the i18n facilities.
 */
public class Localized {
  protected static final AtomicBoolean initialized = new AtomicBoolean(false);
  protected static volatile ResourceBundleWrapper bundle;
  public static final String BASENAME = "de.ks.i18n.Translation";

  /**
   * Use this method in order to notify possible listeners and replace the resource bundle.
   *
   * @param locale
   */
  public static void changeLocale(Locale locale) {
    Locale oldLocale = Locale.getDefault();
    Locale.setDefault(locale);
    initialize();
    CDI.current().select(EventBus.class).get().post(new LanguageChangedEvent(oldLocale, locale));
  }

  protected synchronized static void initialize() {
    Locale locale = Locale.getDefault();
    String path = BASENAME + "_" + locale.getLanguage() + ".properties";
    bundle = new ResourceBundleWrapper(ResourceBundle.getBundle(BASENAME, locale, new UTF8Control()), path);
    initialized.set(true);
  }

  /**
   * @return the currently used bundle to use for eg. JavaFX loaders etc.
   */
  public static ResourceBundleWrapper getBundle() {
    if (!initialized.get()) {
      initialize();
    }
    return bundle;
  }

  /**
   * Use this method to get a translation for a key.
   * The key "hello.world" is stored like that:
   * hello.world=Hello {0}{1}
   * And the corresponding method parameters will be:
   * "hello.world", "world", "!"
   * Which will result in:
   * Hello world!
   * If you add a colon ":" to the end of the string it is ignored.
   * This is quite useful for input fields.
   *
   * @param key
   * @param args
   * @return
   */
  public static String get(String key, Object... args) {
    String string = getBundle().getString(key);

    for (int i = 0; i < args.length; i++) {
      Object arg = args[i];
      string = string.replace("{" + i + "}", arg.toString());
    }
    return string;
  }

  public static String get(Field field) {
    String key = field.getDeclaringClass().getName();
    key += field.getName();
    return get(key);
  }
}
