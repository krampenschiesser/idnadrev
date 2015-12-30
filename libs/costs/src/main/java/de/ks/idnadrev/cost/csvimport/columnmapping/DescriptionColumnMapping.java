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

package de.ks.idnadrev.cost.csvimport.columnmapping;

import de.ks.idnadrev.cost.entity.Booking;
import de.ks.standbein.reflection.PropertyPath;

public class DescriptionColumnMapping extends BookingColumnMapping<String> {
  public DescriptionColumnMapping(int column) {
    super(column, PropertyPath.ofTypeSafe(Booking.class, b -> b.setDescription(null)));
  }

  @Override
  protected String transform(String content) {
    return content;
  }
}