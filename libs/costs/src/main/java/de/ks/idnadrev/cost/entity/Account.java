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
import de.ks.flatadocdb.annotation.ToMany;
import de.ks.flatadocdb.entity.NamedEntity;

import java.util.LinkedList;
import java.util.List;

@Entity
public class Account extends NamedEntity {
  private static final long serialVersionUID = 1L;
  protected String owner;

  @ToMany
  protected List<Booking> bookings = new LinkedList<>();

  protected Account() {
    super(null);
  }

  public Account(String name) {
    super(name);
  }

  public Account addBooking(Booking booking) {
    bookings.add(booking);
    return this;
  }
}
