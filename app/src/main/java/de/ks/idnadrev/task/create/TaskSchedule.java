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
package de.ks.idnadrev.task.create;

import de.ks.BaseController;
import de.ks.idnadrev.entity.Task;
import de.ks.persistence.PersistentWork;
import de.ks.scheduler.Schedule;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.ResourceBundle;

public class TaskSchedule extends BaseController<Task> {
  private static final Logger log = LoggerFactory.getLogger(TaskSchedule.class);

  @FXML
  DatePicker proposedWeek;
  @FXML
  CheckBox useProposedWeekDay;
  @FXML
  DatePicker dueDate;
  @FXML
  TextField dueTime;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    proposedWeek.disableProperty().bind(dueDate.valueProperty().isNotNull());
    useProposedWeekDay.disableProperty().bind(dueDate.valueProperty().isNotNull());

    dueDate.disableProperty().bind(proposedWeek.valueProperty().isNotNull());
    dueTime.disableProperty().bind(proposedWeek.valueProperty().isNotNull());
  }

  @Override
  public void duringSave(Task model) {
    PersistentWork.run(em -> em.flush());
    if (model.getSchedule() == null) {
      model.setSchedule(new Schedule());
    }
    PersistentWork.run(em -> em.flush());
    Schedule schedule = model.getSchedule();
    PersistentWork.run(em -> em.flush());

    LocalDate date = this.dueDate.getValue();
    if (date != null) {
      schedule.setScheduledDate(date);

      if (dueTime.getText() != null) {
        try {
          LocalTime localTime = LocalTime.parse(dueTime.textProperty().getValueSafe());
          schedule.setScheduledTime(localTime);
        } catch (DateTimeParseException e) {
          log.error("Could not parse local time {}", dueTime.getText(), e);
        }
      }
    } else if (proposedWeek.getValue() != null) {
      LocalDate proposedWeekDay = proposedWeek.getValue();
      WeekFields weekFields = WeekFields.of(Locale.getDefault());
      schedule.setProposedWeek(proposedWeekDay.get(weekFields.weekOfWeekBasedYear()));
      if (useProposedWeekDay.isSelected()) {
        schedule.setProposedWeekDay(proposedWeekDay.getDayOfWeek());
      }
    }
  }
}
