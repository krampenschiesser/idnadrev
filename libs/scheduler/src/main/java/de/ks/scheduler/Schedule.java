package de.ks.scheduler;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.persistence.converter.LocalDateConverter;
import de.ks.persistence.converter.LocalTimeConverter;
import de.ks.persistence.entity.AbstractPersistentObject;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
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

  @Enumerated(value = EnumType.STRING)
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
    this.scheduledTime = scheduledTime.truncatedTo(ChronoUnit.MINUTES);
    ;
    return this;
  }

  public RepetitionType getRepetition() {
    return repetition;
  }

  public Schedule setRepetition(RepetitionType repetition) {
    this.repetition = repetition;
    return this;
  }
}
