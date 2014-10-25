/**
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
package de.ks.option.properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ks.option.OptionSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Alternative;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

@Alternative
public class PropertiesOptionSource implements OptionSource {
  private static final Logger log = LoggerFactory.getLogger(PropertiesOptionSource.class);
  public static final String PROPERTIES_FILENAME = "properties";

  private static final ObjectMapper mapper = new ObjectMapper();

  protected final Properties properties = new Properties();

  @PostConstruct
  protected void load() {
    String path = getPropertiesFile();
    File file = new File(path);
    if (file.exists()) {
      try (FileInputStream fileInputStream = new FileInputStream(file)) {
        properties.load(fileInputStream);
      } catch (IOException e) {
        throw new RuntimeException("Could not load properties from " + path);
      }
    }
  }

  protected static String getPropertiesFile() {
    String workingDir = System.getProperty("user.dir");
    if (workingDir.endsWith("bin")) {
      File parentFile = new File(workingDir).getParentFile();
      return parentFile.getPath() + File.separator + "options." + PROPERTIES_FILENAME;
    } else {
      return workingDir + File.separator + "options." + PROPERTIES_FILENAME;
    }
  }

  public static void cleanup() {
    String path = getPropertiesFile();
    if (new File(path).exists()) {
      try {
        Files.delete(Paths.get(path));
      } catch (IOException e) {
        log.error("Could not clean up {}", path, e);
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T readOption(String path) {
    String stringValue = (String) properties.get(path);
    String className = (String) properties.get(path + "_clazz");

    if (stringValue == null) {
      return null;
    }

    try {
      Class<?> valueClass = Class.forName(className);
      Object o = mapper.readValue(stringValue, valueClass);
      return (T) o;
    } catch (ClassNotFoundException | IOException e) {
      log.error("Could not get option {}", path, e);
      return null;
    }
  }

  @Override
  public void saveOption(String path, Object value) {
    try {
      String valueAsString = mapper.writeValueAsString(value);
      properties.put(path, valueAsString);
      properties.put(path + "_clazz", value.getClass().getName());
      try (FileOutputStream stream = new FileOutputStream(new File(getPropertiesFile()))) {
        properties.store(stream, "");
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not set option '" + path + "' and value=" + value, e);
    }
  }
}
