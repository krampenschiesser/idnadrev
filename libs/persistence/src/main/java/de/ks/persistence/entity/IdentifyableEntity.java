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
package de.ks.persistence.entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public interface IdentifyableEntity {
  static ThreadLocal<MethodHandle> handle = ThreadLocal.withInitial(IdentifyableEntity::resolveHandle);

  String getIdPropertyName();

  Object getIdValue();

  default MethodHandle getIdHandle() {
    return handle.get();
  }

  static MethodHandle resolveHandle() {
    try {
      return MethodHandles.lookup().findVirtual(IdentifyableEntity.class, "getIdValue", MethodType.methodType(Object.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
