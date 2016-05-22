/*
 * Copyright [2016] [Christian Loehnert]
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
package de.ks.idnadrev.task.cron;

import java.time.LocalDate;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class WeekHelper {
  WeekFields weekFields = WeekFields.of(Locale.getDefault());

  public int getWeek(LocalDate date) {
    TemporalField temporalField = weekFields.weekOfWeekBasedYear();
    return date.get(temporalField);
  }

  public LocalDate getFirstDayOfWeek(int year, int week) {
    TemporalField temporalField = weekFields.weekOfYear();
    LocalDate date = LocalDate.ofYearDay(year, 1);
    date = date.with(temporalField, week);
    date = date.with(weekFields.dayOfWeek(), 1);
    return date;
  }
}
