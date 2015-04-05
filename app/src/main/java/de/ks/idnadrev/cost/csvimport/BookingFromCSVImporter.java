/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.idnadrev.cost.csvimport;

import de.ks.idnadrev.cost.csvimport.columnmapping.BookingColumnMapping;
import de.ks.idnadrev.entity.cost.Booking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BookingFromCSVImporter {
  protected final String separator;
  protected final List<BookingColumnMapping<?>> mappings = new ArrayList<>();

  public BookingFromCSVImporter(String separator, BookingColumnMapping<?>... mappings) {
    this(separator, Arrays.asList(mappings));
  }

  public BookingFromCSVImporter(String separator, Collection<BookingColumnMapping<?>> mappings) {
    this.separator = separator;
    this.mappings.addAll(mappings);
  }

  public List<Booking> createBookings(List<String> lines) {
    return lines.stream().map(this::createBooking).collect(Collectors.toList());
  }

  public Booking createBooking(String line) {
    String quote = Pattern.quote(separator);
    String[] split = line.split(quote);

    Booking booking = new Booking();

    for (BookingColumnMapping mapping : mappings) {
      mapping.apply(booking, split);
    }

    return booking;
  }
}
