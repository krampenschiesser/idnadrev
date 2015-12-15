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

package de.ks.idnadrev.cost.bookingview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class BookingViewDS implements DataSource<BookingViewModel> {
  private static final Logger log = LoggerFactory.getLogger(BookingViewDS.class);
  private BookingLoadingHint loadingHint;

  @Override
  public BookingViewModel loadModel(Consumer<BookingViewModel> furtherProcessing) {
    if (loadingHint == null) {
      return new BookingViewModel();
    } else {

      List<Booking> bookings = PersistentWork.from(Booking.class, (root, query, builder) -> {
        loadingHint.applyFilter(query, builder, root, true);
      }, null);
      if (!bookings.isEmpty()) {
        Booking totalBefore = getTotalBefore(bookings.get(0).getBookingTime());
        bookings = new ArrayList<>(bookings);

        Booking totalAfter = getTotalBeforeIncluding(bookings.get(bookings.size() - 1).getBookingTime());

        double sum = bookings.stream().mapToDouble(Booking::getAmount).sum();
        Account account = PersistentWork.forName(Account.class, loadingHint.getAccountName());
        Booking sumBooking = new Booking(account, sum, false).setBookingTime(totalAfter.getBookingTime()).setDescription(Localized.get("sum"));

        bookings.add(0, totalBefore);
        bookings.add(sumBooking);
        bookings.add(totalAfter);
      }
      return new BookingViewModel(0D, bookings);
    }
  }

  private Booking getTotalBeforeIncluding(LocalDateTime date) {
    return getTotal(date, true);
  }

  private Booking getTotalBefore(LocalDateTime date) {
    return getTotal(date, false);
  }

  private Booking getTotal(LocalDateTime date, boolean includeDate) {
    return PersistentWork.read(em -> {
      CriteriaBuilder builder = em.getCriteriaBuilder();

      CriteriaQuery<Double> query = builder.createQuery(Double.class);
      Root<Booking> from = query.from(Booking.class);
      loadingHint.applyFilter(query, builder, from, false);
      query.select(builder.sum(from.get(BookingLoadingHint.KEY_AMOUNT)));
      List<Predicate> restriction = new LinkedList<>();
      restriction.add(query.getRestriction());
      if (includeDate) {
        restriction.add(builder.lessThanOrEqualTo(from.get(BookingLoadingHint.KEY_TIME), date));
      } else {
        restriction.add(builder.lessThan(from.get(BookingLoadingHint.KEY_TIME), date));
      }
      query.where(restriction.toArray(new Predicate[2]));

      Double result = em.createQuery(query).getSingleResult();
      log.debug("Sum of bookins before {}: {}", date, result);
      Account account = PersistentWork.forName(Account.class, loadingHint.getAccountName());
      return new Booking(account, result == null ? 0D : result, false).setBookingTime(date).setDescription(Localized.get("total"));
    });
  }

  @Override
  public void saveModel(BookingViewModel model, Consumer<BookingViewModel> beforeSaving) {

  }

  @Override
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint instanceof BookingLoadingHint) {
      loadingHint = ((BookingLoadingHint) dataSourceHint);
    } else {
      loadingHint = null;
    }
  }
}
