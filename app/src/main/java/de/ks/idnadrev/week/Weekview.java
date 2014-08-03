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

package de.ks.idnadrev.week;

import de.ks.BaseController;
import de.ks.calendar.week.WeekView;
import de.ks.calendar.week.WeekViewEntry;
import de.ks.idnadrev.entity.Task;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;

public class Weekview extends BaseController<List<Task>> {
  @FXML
  StackPane root;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    WeekView weekView = new WeekView();
    root.getChildren().add(weekView.getRootPane());

    controller.getJavaFXExecutor().submit(() -> weekView.getScrollPane().setVvalue(0.3));

    LocalDate firstDayOfWeek = weekView.getFirstDayOfWeek();
    for (int i = 0; i < 7; i++) {
      LocalDate now = firstDayOfWeek.plusDays(i);
      LocalTime time = LocalTime.of(6 + i, 0);

      int minutes = Math.max(15, ThreadLocalRandom.current().nextInt(12) * 15);
      Duration duration = Duration.ofMinutes(minutes);
      LocalDateTime localDateTime = LocalDateTime.of(now, time);
      weekView.getEntries().add(new WeekViewEntry("test entry" + i + " " + minutes + "m", localDateTime, duration));
    }
  }
}
