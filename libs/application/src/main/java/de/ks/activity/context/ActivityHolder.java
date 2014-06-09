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

package de.ks.activity.context;

import de.ks.activity.ActivityCfg;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ActivityHolder {
  protected final String id;
  protected final Map<Class<?>, StoredBean> objectStore = new ConcurrentHashMap<>();
  protected final AtomicInteger count = new AtomicInteger(0);
  protected ActivityCfg activityCfg;

  public ActivityHolder(String id) {
    this.id = id;
    count.incrementAndGet();
  }

  public StoredBean getStoredBean(Class<?> key) {
    return objectStore.get(key);
  }

  public void put(Class<?> key, StoredBean storedBean) {
    objectStore.putIfAbsent(key, storedBean);
  }

  public Map<Class<?>, StoredBean> getObjectStore() {
    return objectStore;
  }

  public AtomicInteger getCount() {
    return count;
  }

  public String getId() {
    return id;
  }
}
