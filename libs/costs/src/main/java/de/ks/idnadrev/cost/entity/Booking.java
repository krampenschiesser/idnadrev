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

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.ks.flatadocdb.annotation.Entity;
import de.ks.flatadocdb.annotation.ToOne;
import de.ks.flatadocdb.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
public class Booking extends BaseEntity {
  private static final long serialVersionUID = 1L;
  protected BigDecimal amount;
  protected String internalIdentifier;
  protected String description;

  @ToOne
  protected Account account;

  protected String category;
  protected LocalDateTime bookingTime;

  @JsonIgnore
  protected boolean total;

  public Booking() {
    internalIdentifier = UUID.randomUUID().toString();
  }

  public Booking(Account account, BigDecimal amount) {
    this(account, amount, true);
  }

  public Booking(Account account, BigDecimal amount, boolean book) {
    this.account = account;
    this.amount = amount;
    if (book) {
      account.addBooking(this);
    }
    internalIdentifier = UUID.randomUUID().toString();
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public Booking setAmount(BigDecimal amount) {
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

  public Booking setBookingLocalTime(LocalTime time) {
    if (bookingTime == null) {
      bookingTime = LocalDateTime.of(LocalDate.of(1, 1, 1), time);
    } else {
      bookingTime = LocalDateTime.of(bookingTime.toLocalDate(), time);
    }
    return this;
  }

  public Booking setBookingLocalDate(LocalDate date) {
    if (bookingTime == null) {
      bookingTime = LocalDateTime.of(date, LocalTime.of(0, 0));
    } else {
      bookingTime = LocalDateTime.of(date, bookingTime.toLocalTime());
    }
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

  public boolean isTotal() {
    return total;
  }

  public void setTotal(boolean isTotal) {
    this.total = isTotal;
  }

  public void setAccount(Account account) {
    this.account = account;
  }
//
//  @Override
//  public String getIdPropertyName() {
//    return "internalIdentifier";
//  }
//
//  @Override
//  public Object getIdValue() {
//    return internalIdentifier;
//  }

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
