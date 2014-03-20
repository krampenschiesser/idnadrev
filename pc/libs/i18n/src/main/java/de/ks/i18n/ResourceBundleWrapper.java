/*
 * Copyright [2014] [Christian Loehnert]
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

/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger log = LoggerFactory.getLogger(ResourceBundleWrapper.class);
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
      log.warn("Key \"" + key + "\" not found in properties file:" + path);
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
        log.warn("Key \"" + key + "\" not found in properties file:" + path);
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
