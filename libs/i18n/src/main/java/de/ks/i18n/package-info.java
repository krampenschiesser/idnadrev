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

/**
 * <pre>
 * Provides basic access to i18n facilities.
 *
 * The {@link de.ks.i18n.Localized} class is your main entry point here.
 *
 * The {@link java.util.Locale#getDefault()} is always used as the current language.
 * Language can be changed via {@link de.ks.i18n.Localized#changeLocale(java.util.Locale)}
 *
 * With changing the language the event {@link de.ks.i18n.event.LanguageChangedEvent }
 * is thrown in order to notify possible listeners (eg. labels)
 *
 * Property files must be in the following package:
 * "de.ks.i18n"
 * Naming convention is: Translation_en.properties
 *
 * Basic usage: {@link de.ks.i18n.Localized#get(String, Object...)}
 * The key "hello.world" is stored like that:
 *  hello.world=Hello {0}{1}
 * And the corresponding method call will be:
 *  "hello.world", "world", "!"
 * Which will result in:
 *  Hello world!
 * If you add a colon ":" to the end of the string it is ignored.
 * This is quite useful for input fields.
 * </pre>
 */

package de.ks.i18n;
