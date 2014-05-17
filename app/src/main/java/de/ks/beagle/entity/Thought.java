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

package de.ks.beagle.entity;

import de.ks.beagle.thought.ThoughtOptions;
import de.ks.editor.Detailed;
import de.ks.option.Options;
import de.ks.persistence.entity.NamedPersistentObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.LocalDate;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */
@Entity
public class Thought extends NamedPersistentObject<Thought> {
  private static final long serialVersionUID = 1L;

  @Column(length = 4096)
  @Detailed
  protected String description;
  protected LocalDate postponedDate;

  public Thought() {
  }

  public Thought(String name) {
    super(name);
  }

  public Thought setDescription(String description) {
    this.description = description;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public String getShortDescription() {
    if ((description != null) && (description.length() > 50)) {
      return description.substring(0, 50);
    } else {
      return description;
    }
  }

  public void postPone() {
    ThoughtOptions thoughtOptions = Options.get(ThoughtOptions.class);
    int daysToPostpone = thoughtOptions.getDaysToPostpone();
    postPone(daysToPostpone);
  }

  public void postPone(int daysToPostPone) {
    postponedDate = LocalDate.now().plusDays(daysToPostPone);
  }

  public boolean isPostponed() {
    if (postponedDate != null) {
      LocalDate now = LocalDate.now();
      return now.isBefore(postponedDate);
    }
    return false;
  }
}