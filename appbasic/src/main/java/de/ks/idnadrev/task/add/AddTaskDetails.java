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
package de.ks.idnadrev.task.add;

import de.ks.fxcontrols.weekview.WeekHelper;
import de.ks.idnadrev.task.Task;
import de.ks.idnadrev.task.cron.CronTab;
import de.ks.idnadrev.task.cron.ProposedWeek;
import de.ks.idnadrev.task.cron.ProposedWeekDay;
import de.ks.idnadrev.util.TimePicker;
import de.ks.standbein.BaseController;
import de.ks.standbein.validation.validators.IntegerRangeValidator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.reactfx.EventStreams;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

public class AddTaskDetails extends BaseController<Task> {
  @FXML
  TextField delegation;
  @FXML
  DatePicker scheduledDate;
  @FXML
  TextField proposedWeekEditor;
  @FXML
  DatePicker proposedWeek;
  @FXML
  DatePicker proposedDay;
  @FXML
  ComboBox<DayOfWeek> dayOfWeek;
  @FXML
  TimePicker scheduledTimeController;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    delegation.textProperty().bindBidirectional(store.getBinding().getStringProperty(Task.class, Task::getDelegation));

    ObservableList<DayOfWeek> weekDays = FXCollections.observableArrayList(DayOfWeek.values());
    weekDays.add(0, null);
    dayOfWeek.setItems(weekDays);

    scheduledDate.disableProperty().bind(proposedWeek.valueProperty().isNotNull().or(proposedDay.valueProperty().isNotNull()));
    scheduledTimeController.getTimeEditor().disableProperty().bind(proposedWeek.valueProperty().isNotNull().or(proposedDay.valueProperty().isNotNull()));

    proposedWeek.disableProperty().bind(scheduledDate.valueProperty().isNotNull().or(proposedDay.valueProperty().isNotNull()));
    proposedWeekEditor.disableProperty().bind(scheduledDate.valueProperty().isNotNull().or(proposedDay.valueProperty().isNotNull()).or(dayOfWeek.valueProperty().isNotNull()));
    dayOfWeek.disableProperty().bind(scheduledDate.valueProperty().isNotNull().or(proposedDay.valueProperty().isNotNull()));

    proposedDay.disableProperty().bind(scheduledDate.valueProperty().isNotNull().or(proposedWeek.valueProperty().isNotNull()));

    Arrays.asList(scheduledDate, proposedWeek, proposedDay).forEach(datePicker -> EventStreams.valuesOf(datePicker.getEditor().textProperty()).filter(t -> t == null || t.trim().isEmpty()).subscribe(b -> datePicker.setValue(null)));

    EventStreams.valuesOf(proposedWeek.valueProperty()).subscribe(this::setProposedWeek);
    EventStreams.valuesOf(proposedWeekEditor.textProperty()).subscribe(this::setProposedWeek);

    validationRegistry.registerValidator(proposedWeekEditor, new IntegerRangeValidator(localized, 1, 54));
  }

  private void setProposedWeek(String week) {
    if (week != null && !week.trim().isEmpty()) {
      try {
        int i = Integer.parseInt(week);

        LocalDate firstDayOfWeek = new WeekHelper().getFirstDayOfWeek(LocalDate.now().getYear(), i);
        proposedWeek.setValue(firstDayOfWeek);
      } catch (NumberFormatException e) {
        //ok;
      }
    }
  }

  private void setProposedWeek(LocalDate localDate) {
    if (localDate == null) {
      proposedWeekEditor.setText("");
    } else {

      int week = new WeekHelper().getWeek(localDate);
      proposedWeekEditor.setText(String.valueOf(week));
    }
  }

  @Override
  protected void onRefresh(Task model) {
    super.onRefresh(model);
    CronTab cronTab = model.getCronTab();
    if (cronTab != null) {
      applyCron(cronTab);
    }
  }

  private void applyCron(CronTab cronTab) {
    Optional<LocalDateTime> dateTime = cronTab.getDateTime();
    if (dateTime.isPresent()) {
      scheduledDate.setValue(dateTime.get().toLocalDate());
      scheduledTimeController.setTime(dateTime.get().toLocalTime());
    }
    Optional<LocalDate> proposedDate = cronTab.getProposedDate();
    if (proposedDate.isPresent()) {
      proposedDay.setValue(proposedDate.get());
    }
    Optional<ProposedWeek> proposedWeek = cronTab.getProposedWeek();
    if (proposedWeek.isPresent()) {
      this.proposedWeek.setValue(proposedWeek.get().getMonday());
    }
    Optional<ProposedWeekDay> proposedWeekDay = cronTab.getProposedWeekDay();
    if (proposedWeekDay.isPresent()) {
      this.proposedWeek.setValue(proposedWeekDay.get().getMonday());
      dayOfWeek.getSelectionModel().select(proposedWeekDay.get().getDayOfWeek());
    }
  }

  @Override
  public void duringSave(Task model) {
    super.duringSave(model);

//    model.getCronTab().
  }
}
