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
package de.ks.idnadrev.cost.bookingview;

import de.ks.idnadrev.entity.cost.Account;
import de.ks.idnadrev.entity.cost.Booking;
import de.ks.reflection.PropertyPath;

import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

public class BookingLoadingHint {
  public static final String KEY_ACCOUNT = PropertyPath.property(Booking.class, b -> b.getAccount());
  public static final String KEY_TIME = PropertyPath.property(Booking.class, b -> b.getBookingTime());
  public static final String KEY_CATEGORY = PropertyPath.property(Booking.class, b -> b.getCategory());
  public static final String KEY_DESCRIPTION = PropertyPath.property(Booking.class, b -> b.getDescription());
  public static final String KEY_AMOUNT = PropertyPath.property(Booking.class, b -> b.getAmount());

  private String accountName;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private Double amount;
  private String description;
  private String category;

  public BookingLoadingHint(String accountName) {
    this.accountName = accountName;
    startDate = LocalDateTime.of(LocalDate.now().minusDays(30), LocalTime.of(0, 0));
    endDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59));
  }

  public String getAccountName() {
    return accountName;
  }

  public BookingLoadingHint setAccountName(String accountName) {
    this.accountName = accountName;
    return this;
  }

  public LocalDateTime getStartDate() {
    return startDate;
  }

  public BookingLoadingHint setStartDate(LocalDate startDate) {
    this.startDate = LocalDateTime.of(startDate, LocalTime.of(0, 0));
    return this;
  }

  public LocalDateTime getEndDate() {
    return endDate;
  }

  public BookingLoadingHint setEndDate(LocalDate endDate) {
    this.endDate = LocalDateTime.of(endDate, LocalTime.of(23, 59));
    return this;
  }

  public Double getAmount() {
    return amount;
  }

  public BookingLoadingHint setAmount(Double amount) {
    this.amount = amount;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public BookingLoadingHint setDescription(String description) {
    this.description = description;
    return this;
  }

  public BookingLoadingHint setCategory(String category) {
    this.category = category;
    return this;
  }

  public String getCategory() {
    return category;
  }

  public void applyFilter(CriteriaQuery<?> query, CriteriaBuilder builder, Root<Booking> root, boolean filterDateRange) {
    ArrayList<Predicate> predicates = new ArrayList<>();

    Join<Booking, Account> account = root.join(KEY_ACCOUNT);
    account.on(builder.equal(account.get("name"), accountName));

    if (filterDateRange) {
      predicates.add(builder.and(//
              builder.greaterThanOrEqualTo(root.get(KEY_TIME), startDate),//
              builder.lessThan(root.get(KEY_TIME), endDate)));
    }

    if (category != null) {
      predicates.add(builder.like(root.get(KEY_CATEGORY), "%" + category.trim() + "%"));
    }
    if (description != null) {
      predicates.add(builder.like(root.get(KEY_DESCRIPTION), "%" + description.trim() + "%"));
    }
    if (amount != null) {
      double begin = amount - 0.001D;
      double end = amount + 0.001D;
      predicates.add(builder.greaterThan(root.get(KEY_AMOUNT), begin));
      predicates.add(builder.lessThan(root.get(KEY_AMOUNT), end));
    }

    query.where(predicates.toArray(new Predicate[predicates.size()]));
    if (filterDateRange) {
      Order asc = builder.asc(root.get(KEY_TIME));
      query.orderBy(asc);
    }
  }
}
