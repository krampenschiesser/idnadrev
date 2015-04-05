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

import de.ks.idnadrev.cost.bookingview.BookingViewModel;

public class ImporterBookingViewModel extends BookingViewModel {
  protected StringBuilder errors = new StringBuilder();

  public String getErrors() {
    return errors.toString();
  }

  public void addError(Exception e) {
    errors.append(e.getMessage()).append("\n");
  }

  public void addError(Exception e, String line) {
    errors.append("Cannot parse line \"").append(line).append("\"\n\tReason: ").append(e.getMessage()).append("\n");
  }
}
