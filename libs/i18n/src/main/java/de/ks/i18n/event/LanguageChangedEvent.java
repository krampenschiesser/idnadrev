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

package de.ks.i18n.event;


import java.util.Locale;

/**
 * Indicates a language change to possible listeners.
 */
public class LanguageChangedEvent {
  Locale oldLocale;
  Locale newLocale;

  public LanguageChangedEvent(Locale oldLocale, Locale newLocale) {
    this.oldLocale = oldLocale;
    this.newLocale = newLocale;
  }

  public Locale getNewLocale() {
    return newLocale;
  }

  public void setNewLocale(Locale newLocale) {
    this.newLocale = newLocale;
  }

  public Locale getOldLocale() {
    return oldLocale;
  }

  public void setOldLocale(Locale oldLocale) {
    this.oldLocale = oldLocale;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    LanguageChangedEvent that = (LanguageChangedEvent) o;

    if (newLocale != null ? !newLocale.equals(that.newLocale) : that.newLocale != null) {
      return false;
    }
    if (oldLocale != null ? !oldLocale.equals(that.oldLocale) : that.oldLocale != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = oldLocale != null ? oldLocale.hashCode() : 0;
    result = 31 * result + (newLocale != null ? newLocale.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "LanguageChangedEvent{" +
            "oldLocale=" + oldLocale +
            ", newLocale=" + newLocale +
            '}';
  }
}
