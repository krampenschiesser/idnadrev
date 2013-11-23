package de.ks.i18n;

/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;


/**
 * Ignores ":" and "=" at the end of a string.
 * Also uses the UTF8Control
 */
class ResourceBundleWrapper extends ResourceBundle {

  private static final Logger log = LogManager.getLogger(ResourceBundleWrapper.class);
  private final ResourceBundle bundle;
  private final String path;

  public ResourceBundleWrapper(ResourceBundle bundle, String path) {
    this.bundle = bundle;
    this.path = path;
  }

  @Override
  public boolean containsKey(String key) {
    if (key.endsWith(":")) {
      key = key.substring(0, key.length() - 1);
    } else if (key.endsWith("=")) {
      key = key.substring(0, key.length() - 1);
    }
    boolean isContained = bundle.containsKey(key);
    if (!isContained) {
      log.error("Key \"" + key + "\" not found in properties file:" + path);
      return true;
    }
    return isContained;
  }

  @Override
  public Set<String> keySet() {
    return bundle.keySet();
  }

  @Override
  public Enumeration<String> getKeys() {
    return bundle.getKeys();
  }

  @Override
  protected Object handleGetObject(String key) {
    String ending = null;
    if (key.endsWith(":")) {
      ending = ":";
    } else if (key.endsWith("=")) {
      ending = "=";
    }
    if (ending != null) {
      key = key.substring(0, key.length() - ending.length());
    }
    try {
      Method method = ResourceBundle.class.getDeclaredMethod("handleGetObject", String.class);
      method.setAccessible(true);
      Object retval = method.invoke(bundle, key);
      if ((ending != null) && (retval instanceof String)) {
        retval = retval + ending;
      }
      if (retval == null) {
        log.error("Key \"" + key + "\" not found in properties file:" + path);
        return "?" + key + "?";
      }
      return retval;
    } catch (Exception e) {
      log.error("Could not invoke delegate method.", e);
    }
    return null;
  }

  @Override
  public int hashCode() {
    return bundle.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return bundle.equals(obj);
  }

  @Override
  public String toString() {
    return bundle.toString();
  }

  @Override
  public Locale getLocale() {
    return bundle.getLocale();
  }
}
