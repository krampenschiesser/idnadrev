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

public class SampleApp extends Application {
  private static final Logger log = LoggerFactory.getLogger(SampleApp.class);

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("Sample app");

    WeekView weekView = new WeekView("Today");
    weekView.setAppointmentResolver(this::getNextEntries);
    weekView.setOnAppointmentCreation(dateTime -> log.info("Creating new appointment beginning at {}", dateTime));


    StackPane pane = new StackPane(weekView);
    pane.setPrefSize(800, 600);
    Scene scene = new Scene(pane);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private List<WeekViewAppointment> getNextEntries(LocalDate begin, LocalDate end) {
    ThreadLocalRandom random = ThreadLocalRandom.current();

    LinkedList<WeekViewAppointment> retval = new LinkedList<>();
    LocalDate firstDayOfWeek = begin;
    for (int i = 0; i < 7; i++) {
      LocalDate current = firstDayOfWeek.plusDays(i);
      LocalTime time = LocalTime.of(random.nextInt(6, 18), 0);

      int minutes = Math.max(15, random.nextInt(12) * 15);
      Duration duration = Duration.ofMinutes(minutes);
      LocalDateTime localDateTime = LocalDateTime.of(current, time);

      WeekViewAppointment appointment = new WeekViewAppointment("test entry" + i + " " + minutes + "m", localDateTime, duration, btn -> log.info("Clicking on appointment{}", btn.getText()));
      appointment.setChangeStartCallback(newTime -> {
        if (newTime.getHour() > 6 && newTime.getHour() < 22) {
          log.info("{} now starts on {}", appointment.getTitle(), newTime);
          return true;
        } else {
          log.info("Wrong time {}", newTime);
          return false;
        }
      });
      retval.add(appointment);
    }
    return retval;
  }

  public static void main(String[] args) throws Exception {
    launch(args);
  }
}
