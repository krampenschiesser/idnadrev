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

package de.ks.scheduler;

import de.ks.persistence.converter.LocalDateConverter;
import de.ks.persistence.converter.LocalTimeConverter;
import de.ks.persistence.entity.AbstractPersistentObject;

import javax.persistence.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

/**
 *
 */
@Entity
public class Schedule extends AbstractPersistentObject<Schedule> {
  public enum RepetitionType {
    DAILY, WEEKLY, MONTHLY, YEARLY;
  }

  @Column(columnDefinition = "VARCHAR(250)")
  @Convert(converter = LocalDateConverter.class)
  protected LocalDate scheduledDate;//should be done at this date (and time)
  @Column(columnDefinition = "VARCHAR(250)")
  @Convert(converter = LocalTimeConverter.class)
  protected LocalTime scheduledTime;//if null, only date relevant

  @Enumerated(EnumType.STRING)
  protected DayOfWeek proposedWeekDay;//should be done on this week day if possible
  /**
   * should be done in this week @see WeekFields#weekOfWeekBasedYear()
   */
  protected int proposedWeek;
  protected int proposedYear;

  @Enumerated(EnumType.STRING)
  protected RepetitionType repetition = null;

  public Schedule() {
  }

  public Schedule(LocalDate scheduledDate) {
    this(scheduledDate, null);
  }

  public Schedule(LocalDate scheduledDate, RepetitionType repetition) {
    this();
    this.scheduledDate = scheduledDate;
    this.repetition = repetition;
  }

  public boolean isScheduledToday() {
    return isScheduledToday(LocalDate.now());
  }

  protected boolean isScheduledToday(LocalDate now) {
    if (now.isEqual(scheduledDate)) {
      return true;
    } else if (now.isAfter(scheduledDate) && repetition != null) {
      Period periodBetween = scheduledDate.until(now);

      switch (repetition) {
        case DAILY:
          return true;
        case WEEKLY:
          return periodBetween.getDays() % 7 == 0;
        case MONTHLY:
          return now.getDayOfMonth() == scheduledDate.getDayOfMonth();
        case YEARLY:
          return periodBetween.getDays() == 0 && periodBetween.getMonths() == 0;
      }
    }
    return false;
  }

  public boolean isScheduledNow() {
    return isScheduledNow(LocalTime.now());
  }

  protected boolean isScheduledNow(LocalTime now) {
    now = now.truncatedTo(ChronoUnit.MINUTES);
    return now.equals(scheduledTime);
  }

  public LocalDateTime getScheduledDateTime() {
    if (scheduledTime != null) {
      return LocalDateTime.of(scheduledDate, scheduledTime);
    } else {
      LocalTime now = LocalTime.now();
      return LocalDateTime.of(scheduledDate, now);
    }
  }

  public LocalDate getScheduledDate() {
    return scheduledDate;
  }

  public Schedule setScheduledDate(LocalDate scheduledDate) {
    this.scheduledDate = scheduledDate;
    return this;
  }

  public LocalTime getScheduledTime() {
    return scheduledTime;
  }

  public Schedule setScheduledTime(LocalTime scheduledTime) {
    if (scheduledTime == null) {
      this.scheduledTime = null;
    } else {
      this.scheduledTime = scheduledTime.truncatedTo(ChronoUnit.MINUTES);
    }
    return this;
  }

  public RepetitionType getRepetition() {
    return repetition;
  }

  public Schedule setRepetition(RepetitionType repetition) {
    this.repetition = repetition;
    return this;
  }

  public DayOfWeek getProposedWeekDay() {
    return proposedWeekDay;
  }

  public Schedule setProposedWeekDay(DayOfWeek proposedWeekDay) {
    this.proposedWeekDay = proposedWeekDay;
    return this;
  }

  public int getProposedWeek() {
    return proposedWeek;
  }

  public Schedule setProposedWeek(int proposedWeek) {
    this.proposedWeek = proposedWeek;
    if (proposedYear == 0) {
      setProposedYear(Year.now().getValue());
    }
    return this;
  }

  public int getProposedYear() {
    return proposedYear;
  }

  public Schedule setProposedYear(int proposedYear) {
    this.proposedYear = proposedYear;
    return this;
  }
}
