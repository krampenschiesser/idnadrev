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

import de.ks.persistence.converter.LocalDateTimeConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */
@MappedSuperclass
public abstract class AbstractPersistentObject<T extends AbstractPersistentObject<T>> implements Serializable {
  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue
  protected long id;
  @Version
  protected long version;

  @Column(columnDefinition = "TIMESTAMP")
  @Convert(converter = LocalDateTimeConverter.class)
  protected LocalDateTime creationTime;

  @Column(columnDefinition = "TIMESTAMP")
  @Convert(converter = LocalDateTimeConverter.class)
  protected LocalDateTime updateTime;

  protected AbstractPersistentObject() {
    creationTime = LocalDateTime.now();
  }

  public long getId() {
    return id;
  }

  public long getVersion() {
    return version;
  }

  @PreUpdate
  void preUpdate() {
    updateTime = LocalDateTime.now();
  }

  public LocalDateTime getCreationTime() {
    return creationTime;
  }

  public LocalDateTime getUpdateTime() {
    return updateTime;
  }
}
