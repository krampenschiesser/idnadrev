/*
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

package de.ks.beagle.entity;

import de.ks.persistence.entity.NamedPersistentObject;

import javax.persistence.Entity;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */
@Entity
public class WorkType extends NamedPersistentObject<WorkType> {
  private static final long serialVersionUID = 1L;

  protected String description;

  public WorkType() {
  }

  public WorkType(String name) {
    super(name);
  }

  public String getDescription() {
    return description;
  }

  public WorkType setDescription(String description) {
    this.description = description;
    return this;
  }
}
