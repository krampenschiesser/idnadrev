package de.ks.i18n.event;

/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

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
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    LanguageChangedEvent that = (LanguageChangedEvent) o;

    if (newLocale != null ? !newLocale.equals(that.newLocale) : that.newLocale != null) return false;
    if (oldLocale != null ? !oldLocale.equals(that.oldLocale) : that.oldLocale != null) return false;

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
