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
package de.ks.fxcontrols;

import de.ks.fxcontrols.weekview.WeekView;
import de.ks.fxcontrols.weekview.WeekViewAppointment;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class SampleApp extends Application {
  private static final Logger log = LoggerFactory.getLogger(SampleApp.class);

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("Sample app");

    WeekView<Object> weekView = new WeekView<>("Today");
    weekView.setAppointmentResolver(this::getNextEntries);
    weekView.setOnAppointmentCreation((date, time) -> log.info("Creating new appointment beginning at {} {}", date, time));


    StackPane pane = new StackPane(weekView);
    pane.setPrefSize(800, 600);
    Scene scene = new Scene(pane);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private void getNextEntries(LocalDate begin, LocalDate end, Consumer<List<WeekViewAppointment<Object>>> consumer) {
    ThreadLocalRandom random = ThreadLocalRandom.current();

    LinkedList<WeekViewAppointment<Object>> retval = new LinkedList<>();
    LocalDate firstDayOfWeek = begin;
    for (int i = 0; i < 7; i++) {
      LocalDate current = firstDayOfWeek.plusDays(i);
      LocalTime time = LocalTime.of(random.nextInt(6, 18), random.nextInt(3) * 15);

      int minutes = Math.max(15, random.nextInt(12) * 15);
      Duration duration = Duration.ofMinutes(minutes);
      LocalDateTime localDateTime = LocalDateTime.of(current, time);

      BiPredicate<LocalDate, LocalTime> newTimePossiblePredicate = (newDate, newTime) -> {
        if (newTime == null) {
          return true;
        }
        if (newTime.getHour() > 6 && newTime.getHour() < 22) {
          return true;
        } else {
          log.info("Wrong time {}", newTime);
          return false;
        }
      };

      WeekViewAppointment<Object> timedAppointment = new WeekViewAppointment<>("test entry" + i + " " + minutes + "m", localDateTime, duration);
      timedAppointment.setChangeStartCallback((newDate, newTime) -> {
        log.info("{} now starts on {} {}", timedAppointment.getTitle(), newDate, newTime);
      });
      timedAppointment.setNewTimePossiblePredicate(newTimePossiblePredicate);
      retval.add(timedAppointment);
      for (int j = 0; j < random.nextInt(1, 4); j++) {
        WeekViewAppointment<Object> dayAppointment = new WeekViewAppointment<>(j + " test day spanning entry" + i + " " + minutes + "m", localDateTime.toLocalDate(), duration);
        dayAppointment.setChangeStartCallback((newDate, newTime) -> {
          log.info("{} now starts on {} {}", dayAppointment.getTitle(), newDate, newTime);
        });
        dayAppointment.setNewTimePossiblePredicate(newTimePossiblePredicate);
        retval.add(dayAppointment);
      }
    }
    consumer.accept(retval);
  }

  public static void main(String[] args) throws Exception {
    launch(args);
  }
}
