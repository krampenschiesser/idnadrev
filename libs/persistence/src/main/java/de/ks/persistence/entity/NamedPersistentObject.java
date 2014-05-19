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

package de.ks.persistence.entity;


import de.ks.validation.contraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

@MappedSuperclass
public abstract class NamedPersistentObject<T extends NamedPersistentObject<T>> extends AbstractPersistentObject<T> {
  private static final long serialVersionUID = 1L;
  @NotNull
  @NotEmpty
  @Column(nullable = false, unique = true, length = 4096)
  protected String name;

  protected NamedPersistentObject() {

  }

  public NamedPersistentObject(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @SuppressWarnings("unchecked")
  public T setName(String name) {
    this.name = name;
    return (T) this;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof NamedPersistentObject)) {
      return false;
    }
    NamedPersistentObject<?> other = (NamedPersistentObject<?>) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + ": [name=" + name + "]";
  }
}
