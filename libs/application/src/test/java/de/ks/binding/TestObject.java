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
package de.ks.binding;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class TestObject {
  private int integerValue;
  private float floatValue;
  private Boolean boolValue;
  private String stringValue;
  private Timestamp timestamp;
  private Map<String, String> mapValue = new HashMap<>();
  private Set<String> setValue = new HashSet<>();

  public int getIntegerValue() {
    return integerValue;
  }

  public TestObject setIntegerValue(int integerValue) {
    this.integerValue = integerValue;
    return this;
  }

  public float getFloatValue() {
    return floatValue;
  }

  public TestObject setFloatValue(float floatValue) {
    this.floatValue = floatValue;
    return this;
  }

  public Boolean getBoolValue() {
    return boolValue;
  }

  public TestObject setBoolValue(Boolean boolValue) {
    this.boolValue = boolValue;
    return this;
  }

  public String getStringValue() {
    return stringValue;
  }

  public TestObject setStringValue(String stringValue) {
    this.stringValue = stringValue;
    return this;
  }

  public Timestamp getTimestamp() {
    return timestamp;
  }

  public TestObject setTimestamp(Timestamp timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  public Set<String> getSetValue() {
    return setValue;
  }

  public TestObject setSetValue(Set<String> setValue) {
    this.setValue = setValue;
    return this;
  }

  public Map<String, String> getMapValue() {
    return mapValue;
  }

  public TestObject setMapValue(Map<String, String> mapValue) {
    this.mapValue = mapValue;
    return this;
  }
}
