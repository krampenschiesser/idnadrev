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
package de.ks.idnadrev.cost.csvimport.columnmapping;

import de.ks.idnadrev.entity.cost.Booking;
import de.ks.reflection.PropertyPath;

public abstract class BookingColumnMapping<T> {
  private final int column;
  private final PropertyPath bookingPath;

  BookingColumnMapping(int column, PropertyPath bookingPath) {
    this.column = column;
    this.bookingPath = bookingPath;
  }

  public void apply(Booking booking, String[] columns) {
    if (columns.length < column) {
      throw new IllegalArgumentException("not enough columns in line, expected " + columns + " but was " + columns.length);
    }
    T value = transform(columns[column]);
    if (value != null) {
      bookingPath.setValue(booking, value);
    }
  }

  public int getColumn() {
    return column;
  }

  protected abstract T transform(String content);

  public PropertyPath getBookingPath() {
    return bookingPath;
  }
}
