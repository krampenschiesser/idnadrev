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

package de.ks.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
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
  private final File missingKeyFile;

  public ResourceBundleWrapper(ResourceBundle bundle, String path) {
    String tempDir = System.getProperty("java.io.tmpdir");
    String pathname = tempDir + File.separator + "idnadrev_missing_keys.properties";
    File missing = null;
    try {
      missing = new File(pathname);
      if (missing.exists()) {
        missing.delete();
      }
      missing.createNewFile();
    } catch (IOException e) {
      log.error("Could not create tempfile {} for missing properties", pathname, e);
      missing = null;
    }
    this.missingKeyFile = missing;
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
    boolean isContained = getBundle().containsKey(key);
    if (!isContained) {
      log.warn("Key \"{}\" not found in properties missingKeyFile:{}", key, path);
      return true;
    }
    return true;
  }

  @Override
  public Set<String> keySet() {
    return getBundle().keySet();
  }

  @Override
  public Enumeration<String> getKeys() {
    return getBundle().getKeys();
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
      Object retval = method.invoke(getBundle(), key);
      if ((ending != null) && (retval instanceof String)) {
        retval = retval + ending;
      }
      if (retval == null) {
        log.warn("Key \"{}\" not found in properties:{}", key, path);

        appendToMissingKeyFile(key);
        return "?" + key + "?";
      }
      return retval;
    } catch (Exception e) {
      log.error("Could not invoke delegate method.", e);
    }
    return null;
  }

  protected void appendToMissingKeyFile(String key) throws IOException {
    if (missingKeyFile == null) {
      return;
    }
    try (FileWriter writer = new FileWriter(missingKeyFile, true)) {
      StringBuilder builder = new StringBuilder(key);
      int indexOf = key.lastIndexOf(".");
      if (indexOf > 0) {
        builder.append(" = ").append(key.substring(indexOf + 1));
      } else {
        builder.append(" = ").append(key);
      }
      builder.append("\n");
      writer.append(builder.toString());
    }
  }

  @Override
  public int hashCode() {
    return getBundle().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return getBundle().equals(obj);
  }

  @Override
  public String toString() {
    return getBundle().toString();
  }

  @Override
  public Locale getLocale() {
    return getBundle().getLocale();
  }

  public File getMissingKeyFile() {
    return missingKeyFile;
  }

  protected ResourceBundle getBundle() {
    String baseBundleName = bundle.getBaseBundleName();
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    boolean isNext = false;

    String callerClassName = null;
    for (StackTraceElement stackTraceElement : stackTrace) {
      String className = stackTraceElement.getClassName();
      if (className.equals(Thread.class.getName()) || className.equals(ResourceBundle.class.getName()) || className.equals(getClass().getName()) || className.equals(Localized.class.getName())) {
        continue;
      } else if (className.contains("$")) {
        continue;
      } else {
        callerClassName = className;
        break;
      }
    }
    log.trace("Found caller class {}, basename={}", callerClassName, baseBundleName);
    Locale locale = Locale.getDefault();

    String substring = callerClassName.substring(0, callerClassName.lastIndexOf('.') + 1);


    UTF8Control control = new UTF8Control();
    String baseName = substring + Localized.FILENAME;
    URL resource = getClass().getClassLoader().getResource(control.getResourceName(baseName, locale));
    if (resource == null) {
      resource = getClass().getClassLoader().getResource(control.getResourceName(baseName, control.getFallbackLocale(baseName, locale)));
    }

    if (resource != null) {
      ResourceBundle localBundle = ResourceBundle.getBundle(baseName, locale, control);
      log.debug("Found local bundle {}", baseName);
      if (localBundle != null) {
        return localBundle;
      }
    }
    return this.bundle;
  }

}
