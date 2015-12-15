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

import org.apache.commons.lang3.StringUtils;

public class AmountColumnMapping extends BookingColumnMapping<Double> {
  private final boolean useComma;

  public AmountColumnMapping(int column, boolean useComma) {
    super(column, PropertyPath.ofTypeSafe(Booking.class, b -> b.setAmount(0D)));
    this.useComma = useComma;
  }

  @Override
  protected Double transform(String content) {
    if (content == null || content.isEmpty()) {
      return null;
    } else {
      if (useComma) {
        content = StringUtils.replaceEach(content, new String[]{",", "."}, new String[]{".", ","});
      }
      content = StringUtils.remove(content, ",");
      return Double.valueOf(content);
    }
  }
}
