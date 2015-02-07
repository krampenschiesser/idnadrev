/**
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
package de.ks.idnadrev.entity.cost;

import de.ks.persistence.converter.LocalDateTimeConverter;
import de.ks.persistence.entity.AbstractPersistentObject;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Booking extends AbstractPersistentObject<Booking> {
  protected Double amount;
  protected String internalIdentifier;
  protected String description;

  @ManyToOne(optional = false)
  protected Account account;

  protected String category;

  @Column(columnDefinition = "TIMESTAMP")
  @Convert(converter = LocalDateTimeConverter.class)
  protected LocalDateTime bookingTime;

  @Transient
  protected double total;

  protected Booking() {
    internalIdentifier = UUID.randomUUID().toString();
  }

  public Booking(Account account, double amount) {
    this.account = account;
    this.amount = amount;
    account.addBooking(this);
    internalIdentifier = UUID.randomUUID().toString();
  }

  public double getAmount() {
    return amount;
  }

  public Booking setAmount(double amount) {
    this.amount = amount;
    return this;
  }

  public String getInternalIdentifier() {
    return internalIdentifier;
  }

  public Booking setInternalIdentifier(String internalIdentifier) {
    this.internalIdentifier = internalIdentifier;
    return this;
  }

  public LocalDateTime getBookingTime() {
    return bookingTime;
  }

  public Booking setBookingTime(LocalDateTime bookingTime) {
    this.bookingTime = bookingTime;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public Booking setDescription(String description) {
    this.description = description;
    return this;
  }

  public Account getAccount() {
    return account;
  }

  public String getCategory() {
    return category;
  }

  public Booking setCategory(String category) {
    this.category = category;
    return this;
  }

  public double getTotal() {
    return total;
  }

  public Booking setTotal(double total) {
    this.total = total;
    return this;
  }

  @Override
  public String getIdPropertyName() {
    return "internalIdentifier";
  }

  @Override
  public Object getIdValue() {
    return internalIdentifier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Booking)) {
      return false;
    }

    Booking booking = (Booking) o;

    if (!internalIdentifier.equals(booking.internalIdentifier)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return internalIdentifier.hashCode();
  }
}
