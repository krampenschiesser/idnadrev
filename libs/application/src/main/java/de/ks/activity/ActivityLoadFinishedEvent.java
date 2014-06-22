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
package de.ks.activity;

public class ActivityLoadFinishedEvent {
  private final Object model;
  private final long time;

  public ActivityLoadFinishedEvent(Object model) {
    this.model = model;
    this.time = System.currentTimeMillis();
  }

  @SuppressWarnings("unchecked")
  public <T> T getModel() {
    return (T) model;
  }

  public long getTime() {
    return time;
  }
}
