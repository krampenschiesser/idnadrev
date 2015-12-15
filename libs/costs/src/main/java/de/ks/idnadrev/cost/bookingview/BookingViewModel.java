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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookingViewModel {
  protected final List<Booking> bookings = new ArrayList<>();
  protected final double total;

  public BookingViewModel() {
    this(0D, Collections.emptyList());
  }

  public BookingViewModel(double total, List<Booking> bookings) {
    this.total = total;
    this.bookings.addAll(bookings);
  }

  public List<Booking> getBookings() {
    return bookings;
  }

  public double getTotal() {
    return total;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BookingViewModel)) {
      return false;
    }

    BookingViewModel that = (BookingViewModel) o;

    if (Double.compare(that.total, total) != 0) {
      return false;
    }
    if (bookings != null ? !bookings.equals(that.bookings) : that.bookings != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = bookings != null ? bookings.hashCode() : 0;
    temp = Double.doubleToLongBits(total);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("BookingViewModel{");
    sb.append("total=").append(total);
    sb.append(", bookings=").append(bookings);
    sb.append('}');
    return sb.toString();
  }
}
