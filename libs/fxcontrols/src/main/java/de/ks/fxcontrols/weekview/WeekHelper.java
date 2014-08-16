/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
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
package de.ks.fxcontrols.weekview;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;

public class WeekHelper {

  public int getWeek(LocalDate date) {
    return date.get(WeekFields.ISO.weekOfWeekBasedYear());
  }

  public int getWeeksInYear(int year) {
    LocalDate lastInYear = getLastDayOfYear(year);

    int week = lastInYear.get(WeekFields.ISO.weekOfWeekBasedYear());
    if (week == 1) {
      lastInYear = lastInYear.minusWeeks(1);
      week = lastInYear.get(WeekFields.ISO.weekOfWeekBasedYear());
    }
    return week;
  }

  public LocalDate getFirstDayOfYear(int year) {
    return LocalDate.ofYearDay(year, 1);
  }

  public LocalDate getFirstDayOfWeek1InYear(int year) {
    LocalDate firstDayOfYear = getFirstDayOfYear(year);
    LocalDate firstOfWeek = getFirstDayOfWeek(firstDayOfYear);
    LocalDate lastOfWeek = getLastDayOfWeek(firstDayOfYear);

    if (lastOfWeek.getMonth() != Month.JANUARY) {
      firstOfWeek = firstOfWeek.plusWeeks(1);
    } else if (lastOfWeek.getDayOfMonth() < 4) {
      firstOfWeek = firstOfWeek.plusWeeks(1);
    }
    return firstOfWeek;
  }

  public LocalDate getLastDayOfWeek(LocalDate date) {
    return date.plusDays(7 - date.getDayOfWeek().getValue());
  }

  public LocalDate getFirstDayOfWeek(LocalDate date) {
    return date.minusDays(date.getDayOfWeek().getValue() - 1);
  }

  public LocalDate getLastDayOfYear(int year) {
    Year currentYear = Year.of(year);
    LocalDate lastInYear = LocalDate.ofYearDay(year, 365);
    if (currentYear.isLeap()) {
      lastInYear = LocalDate.ofYearDay(year, 366);
    }
    return lastInYear;
  }

  public Month getMonthOfWeek(int year, int week) {
    LocalDate firstDayOfWeek1InYear = getFirstDayOfWeek1InYear(year);
    LocalDate localDate = firstDayOfWeek1InYear.plusWeeks(week - 1);

    LocalDate firstDayOfWeek = getFirstDayOfWeek(localDate);
    LocalDate lastDayOfWeek = getLastDayOfWeek(localDate);

    Month first = firstDayOfWeek.getMonth();
    Month last = lastDayOfWeek.getMonth();
    if (first == last) {
      return first;
    }

    List<Month> months = new ArrayList<>(7);
    for (LocalDate current = firstDayOfWeek; current.compareTo(lastDayOfWeek) < 0; current = current.plusDays(1)) {
      months.add(current.getMonth());
    }

    long countFirst = months.stream().filter(m -> m == first).count();
    long countLast = months.stream().filter(m -> m == last).count();
    if (countFirst > countLast) {
      return first;
    } else {
      return last;
    }
  }

  public LocalDate getFirstDayOfWeek(int year, int week) {
    LocalDate firstDayOfWeek1InYear = getFirstDayOfWeek1InYear(year);
    return firstDayOfWeek1InYear.plusWeeks(week - 1);
  }

  public LocalDate getLastDayOfWeek(int year, int week) {
    LocalDate firstDayOfWeek1InYear = getFirstDayOfWeek1InYear(year);
    return getLastDayOfWeek(firstDayOfWeek1InYear.plusWeeks(week - 1));
  }
}
