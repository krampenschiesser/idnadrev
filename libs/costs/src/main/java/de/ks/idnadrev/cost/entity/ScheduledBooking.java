/*
 * Copyright [2015] [Christian Loehnert]
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

package de.ks.idnadrev.cost.entity;

import de.ks.flatadocdb.annotation.Entity;
import de.ks.flatadocdb.entity.NamedEntity;

@Entity
public class ScheduledBooking extends NamedEntity {
  private static final long serialVersionUID = 1L;
//  @ManyToOne(cascade = CascadeType.ALL)
//  protected Schedule schedule;

  protected double amount;

  protected ScheduledBooking() {
    super(null);
  }

  public ScheduledBooking(String name) {
    super(name);
  }
}