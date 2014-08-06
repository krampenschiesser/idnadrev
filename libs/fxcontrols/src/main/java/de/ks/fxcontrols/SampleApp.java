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
import de.ks.fxcontrols.weekview.WeekViewEntry;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.ThreadLocalRandom;

public class SampleApp extends Application {
  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("Sample app");

    WeekView weekView = new WeekView();

    LocalDate firstDayOfWeek = weekView.getFirstDayOfWeek();
    for (int i = 0; i < 7; i++) {
      LocalDate now = firstDayOfWeek.plusDays(i);
      LocalTime time = LocalTime.of(6 + i, 0);

      int minutes = Math.max(15, ThreadLocalRandom.current().nextInt(12) * 15);
      Duration duration = Duration.ofMinutes(minutes);
      LocalDateTime localDateTime = LocalDateTime.of(now, time);
      weekView.getEntries().add(new WeekViewEntry("test entry" + i + " " + minutes + "m", localDateTime, duration));
    }


    StackPane pane = new StackPane(weekView);
    pane.setPrefSize(800, 600);
    Scene scene = new Scene(pane);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args) throws Exception {
    launch(args);
  }
}
