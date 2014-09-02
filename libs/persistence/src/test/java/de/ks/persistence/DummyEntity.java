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

package de.ks.persistence;

import de.ks.persistence.converter.LocalDateConverter;
import de.ks.persistence.converter.LocalTimeConverter;
import de.ks.persistence.entity.AbstractPersistentObject;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 *
 */
@Entity
public class DummyEntity extends AbstractPersistentObject<DummyEntity> {
  private static final long serialVersionUID = 1L;
  @NotNull
  protected String name;

  @Column(columnDefinition = "TIME")
  @Convert(converter = LocalTimeConverter.class)
  protected LocalTime myTime;

  @Column(columnDefinition = "DATE")
  @Convert(converter = LocalDateConverter.class)
  protected LocalDate myDate;

  protected DummyEntity() {
    //
  }

  public DummyEntity(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public DummyEntity setName(String name) {
    this.name = name;
    return this;
  }

  public LocalDate getMyDate() {
    return myDate;
  }

  public void setMyDate(LocalDate myDate) {
    this.myDate = myDate;
  }

  public LocalTime getMyTime() {
    return myTime;
  }

  public void setMyTime(LocalTime myTime) {
    this.myTime = myTime;
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
    if (!(obj instanceof DummyEntity)) {
      return false;
    }
    DummyEntity other = (DummyEntity) obj;
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
