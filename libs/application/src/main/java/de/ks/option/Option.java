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
package de.ks.option;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ks.persistence.entity.NamedPersistentObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.io.IOException;

@Entity
public class Option extends NamedPersistentObject<Option> {
  private static final Logger log = LoggerFactory.getLogger(Option.class);
  private static final ObjectMapper mapper = new ObjectMapper();

  @Column(length = 4096)
  protected String value;
  protected String valueClassName;

  protected Option() {

  }

  public Option(String path) {
    super(path);
  }

  public String getJSONString() {
    return value;
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
