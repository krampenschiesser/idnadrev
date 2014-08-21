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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class WeekViewAppointment<T> implements Comparable<WeekViewAppointment> {
  protected String title;

  protected LocalDateTime start;
  protected Duration duration;
  protected BiConsumer<Button, T> action;
  protected Consumer<Button> enhancer;
  protected Button node;
  protected Predicate<LocalDateTime> newTimePossiblePredicate;
  protected Consumer<LocalDateTime> changeStartCallback;
  protected T userData;

  public WeekViewAppointment(String title, LocalDateTime start, Duration duration) {
    this.title = title;
    this.start = start;
    this.duration = duration;
  }

  public Control getControl() {
    if (node == null) {
      Button button = new Button(title);
      button.setPrefWidth(Control.USE_COMPUTED_SIZE);
      button.setMaxWidth(Double.MAX_VALUE);
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

  public LocalDateTime getStart() {
    return start;
  }

  public void setStart(LocalDateTime start) {
    this.start = start;
    if (changeStartCallback != null) {
      changeStartCallback.accept(start);
    }
  }

  public Duration getDuration() {
    return duration;
  }

  public WeekViewAppointment setEnhancer(Consumer<Button> enhancer) {
    this.enhancer = enhancer;
    return this;
  }

  public void setChangeStartCallback(Consumer<LocalDateTime> changeStartCallback) {
    this.changeStartCallback = changeStartCallback;
  }

  public Consumer<LocalDateTime> getChangeStartCallback() {
    return changeStartCallback;
  }

  public Predicate<LocalDateTime> getNewTimePossiblePredicate() {
    return newTimePossiblePredicate;
  }

  public void setNewTimePossiblePredicate(Predicate<LocalDateTime> newTimePossiblePredicate) {
    this.newTimePossiblePredicate = newTimePossiblePredicate;
  }

  public boolean contains(LocalDateTime finishTime) {
    Duration between = Duration.between(start, finishTime);
    if (between.isNegative()) {
      return false;
    }
    return between.compareTo(duration) > 0;
  }

  @Override
  public int compareTo(WeekViewAppointment o) {
    return getStart().compareTo(o.getStart());
  }

  @Override
  public String toString() {
    return "WeekViewAppointment{" +
            "title='" + title + '\'' +
            ", start=" + start +
            ", duration=" + duration +
            '}';
  }
}
