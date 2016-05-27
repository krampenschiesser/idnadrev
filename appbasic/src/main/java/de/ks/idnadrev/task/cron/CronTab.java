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

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class CronTab {
  private CronValue minute = new CronValue(0, 59);
  private CronValue hour = new CronValue(0, 23);
  private CronValue dayOfMonth = new CronValue(1, 31);
  private CronValue month = new CronValue(1, 12);
  private CronValue year = new CronValue(2000, 3000);
  private CronValue weekOfYear = new CronValue(1, 53, true);//only together with weekOfYear
  private CronValue dayOfWeek = new CronValue(0, 6, true);//only together with weekOfYear

  public CronTab parse(String input) {
    reset();
    String[] split = StringUtils.split(input, " ");
    if (split.length < 5 || split.length > 7) {
      throw new IllegalArgumentException("Wrong cron length");
    }
    minute.parse(split[0].trim());
    hour.parse(split[1].trim());
    dayOfMonth.parse(split[2].trim());
    month.parse(split[3].trim());
    year.parse(split[4].trim());
    weekOfYear.setNoValue(true);
    dayOfWeek.setNoValue(true);
    if (split.length >= 6) {
      weekOfYear.parse(split[5].trim());
    }
    if (split.length >= 7) {
      dayOfWeek.parse(split[6].trim());
    }
    return this;
  }

  void reset() {
    minute.noValue = true;
    hour.noValue = true;
    dayOfMonth.noValue = true;
    month.noValue = true;
    year.noValue = true;
    weekOfYear.noValue = true;
    dayOfWeek.noValue = true;
  }

  /**
   * @return the proposed date, only if the cron matches: ? ? 26 4 2016 (execute sometime on 2016-04-26
   */
  public Optional<LocalDate> getProposedDate() {
    if (minute.isAny() && hour.isAny()) {
      boolean fixedDate = dayOfMonth.hasSingleValue() && month.hasSingleValue() && year.hasSingleValue();
      if (fixedDate) {
        return Optional.of(LocalDate.of(year.getValue(), month.getValue(), dayOfMonth.getValue()));
      }
    }
    return Optional.empty();
  }

  /**
   * @return the fixed date time, only if the cron matches: 48 23 26 4 2016 (execute sometime on 2016-04-26 23:48
   */
  public Optional<LocalDateTime> getDateTime() {
    boolean fixedDateTime = minute.hasSingleValue() && hour.hasSingleValue() && dayOfMonth.hasSingleValue() && month.hasSingleValue() && year.hasSingleValue();
    if (fixedDateTime) {
      return Optional.of(LocalDateTime.of(year.getValue(), month.getValue(), dayOfMonth.getValue(), hour.getValue(), minute.getValue()));
    }
    return Optional.empty();
  }

  /**
   * @return the next proposed date after the given date.
   */
  public Optional<LocalDate> getNextProposedDate(LocalDate now) {
    if (minute.isAny() && hour.isAny()) {
      int dayValue = dayOfMonth.getClosestValue(now.getDayOfMonth());
      if (dayValue < 0) {
        now = now.plusMonths(1);
      }
      int monthValue = month.getClosestValue(now.getMonthValue());
      if (monthValue < 0) {
        now = now.plusYears(1);
      }
      int yearValue = year.getClosestValue(now.getYear());
      if (yearValue < 0) {
        yearValue--;
      }
      return Optional.of(LocalDate.of(Math.abs(yearValue), Math.abs(monthValue), Math.abs(dayValue)));
    }
    return Optional.empty();
  }

  public Optional<LocalDateTime> getNextDateTime(LocalDateTime now) {
    int minuteValue = minute.getClosestValue(now.getMinute());
    if (minuteValue < 0) {
      now = now.plusHours(1);
    }
    int hourValue = hour.getClosestValue(now.getHour());
    if (hourValue < 0) {
      now = now.plusDays(1);
    }
    int dayValue = dayOfMonth.getClosestValue(now.getDayOfMonth());
    if (dayValue < 0) {
      now = now.plusMonths(1);
    }
    int monthValue = month.getClosestValue(now.getMonthValue());
    if (monthValue < 0) {
      now = now.plusYears(1);
    }
    int yearValue = year.getClosestValue(now.getYear());
    if (yearValue < 0) {
      yearValue--;
    }
    return Optional.of(LocalDateTime.of(Math.abs(yearValue), Math.abs(monthValue), Math.abs(dayValue), Math.abs(hourValue), Math.abs(minuteValue)));
  }

  public Optional<ProposedWeek> getProposedWeek() {
    if (weekOfYear.hasSingleValue() && year.hasSingleValue() && !dayOfWeek.hasSingleValue()) {
      return Optional.of(new ProposedWeek(year.getValue(), weekOfYear.getValue()));
    }
    return Optional.empty();
  }

  public Optional<ProposedWeekDay> getProposedWeekDay() {
    if (weekOfYear.hasSingleValue() && year.hasSingleValue() && dayOfWeek.hasSingleValue()) {
      return Optional.of(new ProposedWeekDay(year.getValue(), weekOfYear.getValue(), dayOfWeek.getValue()));
    }
    return Optional.empty();
  }

  public CronTab setProposedWeekDay(ProposedWeekDay proposedWeekDay) {
    reset();
    year.addValue(proposedWeekDay.getYear());
    weekOfYear.addValue(proposedWeekDay.getWeek());
    dayOfWeek.addValue(proposedWeekDay.getDayOfWeek());
    return this;
  }

  public CronTab setProposedWeek(ProposedWeek proposedWeek) {
    reset();
    year.addValue(proposedWeek.getYear());
    weekOfYear.addValue(proposedWeek.getWeek());
    return this;
  }

  public CronTab setProposedDate(LocalDate date) {
    reset();
    minute.setAny(true);
    hour.setAny(true);
    dayOfMonth.addValue(date.getDayOfMonth());
    month.addValue(date.getMonthValue());
    year.addValue(date.getYear());
    return this;
  }

  public CronTab setScheduledDate(LocalDate date) {
    reset();
    minute.setAny(true);
    hour.setAny(true);
    dayOfMonth.addValue(date.getDayOfMonth());
    month.addValue(date.getMonthValue());
    year.addValue(date.getYear());
    return this;
  }

  public CronTab setScheduledDateTime(LocalDateTime dateTime) {
    reset();
    minute.addValue(dateTime.getMinute());
    hour.addValue(dateTime.getHour());
    dayOfMonth.addValue(dateTime.getDayOfMonth());
    month.addValue(dateTime.getMonthValue());
    year.addValue(dateTime.getYear());
    return this;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append(minute.toString()).append(" ");
    b.append(hour.toString()).append(" ");
    b.append(dayOfMonth.toString()).append(" ");
    b.append(month.toString()).append(" ");
    b.append(year.toString());
    if (!weekOfYear.isNoValue()) {
      b.append(" ").append(weekOfYear.toString());
      if (!dayOfWeek.isNoValue()) {
        b.append(" ").append(dayOfWeek.toString());
      }
    }
    return b.toString();
  }
}
