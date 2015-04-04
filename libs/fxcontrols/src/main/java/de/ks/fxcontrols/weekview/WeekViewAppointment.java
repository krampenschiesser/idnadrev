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

import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class WeekViewAppointment<T> implements Comparable<WeekViewAppointment> {
  protected String styleClass;

  protected String title;

  protected LocalDate startDate;
  protected LocalTime startTime;
  protected Duration duration;
  protected BiConsumer<Button, T> action;
  protected Consumer<Button> enhancer;
  protected Button node;
  protected BiPredicate<LocalDate, LocalTime> newTimePossiblePredicate;
  protected BiConsumer<LocalDate, LocalTime> changeStartCallback;
  protected T userData;

  public WeekViewAppointment(String title, LocalDate startDate, Duration duration) {
    this.title = title;
    this.startDate = startDate;
    this.startTime = null;
    this.duration = duration;
  }

  public WeekViewAppointment(String title, LocalDateTime start, Duration duration) {
    this.title = title;
    this.startDate = start.toLocalDate();
    this.startTime = start.toLocalTime();
    this.duration = duration;
  }

  public Control getControl() {
    if (node == null) {
      Button button = new Button(title);
      button.setPrefWidth(Control.USE_COMPUTED_SIZE);
      button.setMaxWidth(Double.MAX_VALUE);
      button.setWrapText(true);
      button.setTooltip(new Tooltip(title));
      if (enhancer != null) {
        enhancer.accept(button);
      }
      if (action != null) {
        button.setOnAction(e -> action.accept(button, userData));
      }
      node = button;
    }
    return node;
  }

  public T getUserData() {
    return userData;
  }

  public void setUserData(T userData) {
    this.userData = userData;
  }

  public BiConsumer<Button, T> getAction() {
    return action;
  }

  public WeekViewAppointment<T> setAction(BiConsumer<Button, T> action) {
    this.action = action;
    return this;
  }

  public String getTitle() {
    return title;
  }

  public WeekViewAppointment setTitle(String title) {
    this.title = title;
    return this;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public LocalDateTime getStart() {
    if (startTime == null) {
      return null;
    }
    return LocalDateTime.of(startDate, startTime);
  }

  public boolean isSpanningWholeDay() {
    return startTime == null;
  }

  protected void setStartWithoutCallback(LocalDate date, LocalTime time) {
    this.startDate = date;
    this.startTime = time;
  }

  public void setStart(LocalDate date, LocalTime time) {
    this.startDate = date;
    this.startTime = time;
    if (changeStartCallback != null) {
      changeStartCallback.accept(date, time);
    }
  }

  public Duration getDuration() {
    return duration;
  }

  public WeekViewAppointment<T> setDuration(Duration duration) {
    this.duration = duration;
    return this;
  }

  public WeekViewAppointment<T> setEnhancer(Consumer<Button> enhancer) {
    this.enhancer = enhancer;
    return this;
  }

  public void setChangeStartCallback(BiConsumer<LocalDate, LocalTime> changeStartCallback) {
    this.changeStartCallback = changeStartCallback;
  }

  public BiConsumer<LocalDate, LocalTime> getChangeStartCallback() {
    return changeStartCallback;
  }

  public BiPredicate<LocalDate, LocalTime> getNewTimePossiblePredicate() {
    return newTimePossiblePredicate;
  }

  public void setNewTimePossiblePredicate(BiPredicate<LocalDate, LocalTime> newTimePossiblePredicate) {
    this.newTimePossiblePredicate = newTimePossiblePredicate;
  }

  public boolean contains(LocalDateTime finishTime) {
    if (isSpanningWholeDay()) {
      return false;
    }
    Duration between = Duration.between(getStart(), finishTime);
    if (between.isNegative()) {
      return false;
    }
    int comparison = between.compareTo(duration);
    return comparison <= 0;
  }

  public LocalDateTime getEnd() {
    if (isSpanningWholeDay()) {
      return null;
    }
    LocalDateTime end = getStart().plus(duration);
    return end;
  }

  @Override
  public int compareTo(WeekViewAppointment o) {
    int dateComparison = getStartDate().compareTo(o.getStartDate());
    if (dateComparison == 0) {
      LocalTime time1 = getStartTime();
      LocalTime time2 = o.getStartTime();
      if (time1 == null) {
        return 1;
      } else if (time2 == null) {
        return -1;
      } else {
        return time1.compareTo(time2);
      }
    }
    return dateComparison;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof WeekViewAppointment)) {
      return false;
    }

    WeekViewAppointment that = (WeekViewAppointment) o;

    if (!title.equals(that.title)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return title.hashCode();
  }

  @Override
  public String toString() {
    return "WeekViewAppointment{" +
      "title='" + title + '\'' +
      ", startDate=" + startDate +
      ", startTime=" + startTime +
      ", duration=" + duration +
      ", userData=" + userData +
      '}';
  }
}
