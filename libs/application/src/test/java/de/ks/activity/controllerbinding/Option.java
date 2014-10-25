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
package de.ks.activity.controllerbinding;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;

public class Option implements Serializable {
  private static final long serialVersionUID = 1L;
  private static final Logger log = LoggerFactory.getLogger(Option.class);
  private static final ObjectMapper mapper = new ObjectMapper();

  protected String name;
  protected String value;
  protected String valueClassName;

  protected Option() {
    //
  }

  public Option(String path) {
    this.name = path;
  }

  public String getJSONString() {
    return value;
  }

  public String getName() {
    return name;
  }

  public Option setName(String name) {
    this.name = name;
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue() {
    Class<?> valueClass = null;
    try {
      valueClass = Class.forName(valueClassName);
      Object o = mapper.readValue(value, valueClass);
      return (T) o;
    } catch (ClassNotFoundException e) {
      log.error("Could not find class {} for option '{}'", valueClassName, getName());
      return null;
    } catch (IOException e) {
      log.error("Could not deserialize json string. Option= '{}', json='{}'", getName(), value);
      return null;
    }
  }

  public <T> Option setValue(T value) {
    try {
      this.value = mapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      log.error("Could not serialize object to JSON string. Option= '{}', object='{}'", getName(), value);
      throw new RuntimeException(e);
    }
    valueClassName = value.getClass().getName();
    return this;
  }
}
