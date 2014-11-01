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
package de.ks.idnadrev.task.view;

import de.ks.BaseController;
import de.ks.i18n.Localized;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.WorkUnit;
import de.ks.persistence.PersistentWork;
import de.ks.validation.ValidationMessage;
import de.ks.validation.validators.NotEmptyValidator;
import de.ks.validation.validators.TimeHHMMValidator;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class WorkUnitController extends BaseController<List<Task>> {

  @FXML
  protected TableView<WorkUnit> workUnitTable;
  @FXML
  protected TableColumn<WorkUnit, String> endColumn;
  @FXML
  protected TableColumn<WorkUnit, String> startColumn;
  @FXML
  protected TableColumn<WorkUnit, String> duration;
  @FXML
  protected DatePicker date;
  @FXML
  protected TextField start;
  @FXML
  protected Button edit;
  @FXML
  protected Button createNew;
  @FXML
  protected Button delete;
  @FXML
  protected TextField end;
  @FXML
  protected GridPane root;
  protected final SimpleObjectProperty<Task> task = new SimpleObjectProperty<>();
  protected final SimpleBooleanProperty createNewPossible = new SimpleBooleanProperty();
  protected final DateTimeFormatter fullDateTimeFormatter = DateTimeFormatter.ofPattern(Localized.get("fullDate"));
  protected final DateTimeFormatter hoursMinutesFormatter = DateTimeFormatter.ofPattern(Localized.get("duration.format"));

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    startColumn.setCellValueFactory(param -> createTimeStringProperty(param.getValue().getStart()));
    endColumn.setCellValueFactory(param -> createTimeStringProperty(param.getValue().getEnd()));
    duration.setCellValueFactory(param -> {
      SimpleStringProperty retval = new SimpleStringProperty();
      long min = param.getValue().getDuration().toMinutes();
      retval.set(min + "m");
      return retval;
    });

    task.addListener((p, o, n) -> {
      workUnitTable.getItems().clear();
      if (n != null) {
        reload(n);
      }
    });

    ReadOnlyObjectProperty<WorkUnit> selectedItemProperty = workUnitTable.getSelectionModel().selectedItemProperty();
    selectedItemProperty.addListener((p, o, n) -> {
      if (n == null) {
        end.setText("");
        start.setText("");
        date.setValue(LocalDate.now());
      } else {
        start.setText(hoursMinutesFormatter.format(n.getStart()));
        end.setText(hoursMinutesFormatter.format(n.getEnd()));
        date.setValue(n.getStart().toLocalDate());
      }
    });
    validationRegistry.registerValidator(start, new TimeHHMMValidator());
    validationRegistry.registerValidator(end, new TimeHHMMValidator());
    validationRegistry.registerValidator(start, new NotEmptyValidator());
    validationRegistry.registerValidator(end, new NotEmptyValidator());
    Validator<Object> validator = (control, value) -> {
      try {
        LocalTime startTime = LocalTime.parse(start.getText(), hoursMinutesFormatter);
        LocalTime endTime = LocalTime.parse(end.getText(), hoursMinutesFormatter);
        if (startTime.isAfter(endTime)) {
          return ValidationResult.fromMessages(new ValidationMessage("validation.time.before", control, startTime, endTime));
        }
        return null;
      } catch (DateTimeParseException e) {
        return null;
      }
    };
//    validationRegistry.registerValidator(start, validator);
    validationRegistry.registerValidator(end, validator);
    date.setValue(LocalDate.now());

    edit.disableProperty().bind(validationRegistry.invalidProperty().or(selectedItemProperty.isNull()));

    createNew.disableProperty().bind(validationRegistry.invalidProperty().or(createNewPossible.not()));
    delete.disableProperty().bind(selectedItemProperty.isNull().or(store.loadingProperty()));

    start.textProperty().addListener((p, o, n) -> {
      if (n != null && validationRegistry.isValid()) {
        checkStartTimeValidForNew();
      } else {

        try {
          LocalDateTime startTime = getEnteredDate(n);
          LocalDateTime endTime = getEnteredDate(end.getText());
          if (endTime.isBefore(startTime)) {
            String endText = hoursMinutesFormatter.format(startTime.plusMinutes(1));
            end.setText(endText);
          }
        } catch (DateTimeParseException e) {
          //
        }

        createNewPossible.set(false);
        controller.getExecutorService().schedule(() -> {
          controller.getJavaFXExecutor().submit(() -> checkStartTimeValidForNew());
        }, 100, TimeUnit.MILLISECONDS);
      }
    });
  }

  protected void reload(Task n) {
    workUnitTable.getItems().clear();
    PersistentWork.wrap(() -> {
      Task reload = PersistentWork.reload(n);
      workUnitTable.getItems().addAll(reload.getWorkUnits());
    });
  }

  protected void checkStartTimeValidForNew() {
    try {
      LocalDateTime startTime = getEnteredDate(start.getText());
      LocalDateTime endTime = getEnteredDate(end.getText());

      ObservableList<WorkUnit> items = workUnitTable.getItems();
      boolean isNewStartTime = checkNewStartTimePossible(startTime, endTime, items);
      createNewPossible.set(isNewStartTime);
    } catch (DateTimeParseException e) {
      createNewPossible.set(false);
    }
  }

  protected boolean checkNewStartTimePossible(LocalDateTime startTime, LocalDateTime endTime, List<WorkUnit> items) {
    for (WorkUnit workUnit : items) {
      LocalDateTime currentStart = workUnit.getStart().withSecond(0).withNano(0);
      LocalDateTime currentEnd = workUnit.getEnd().withSecond(0).withNano(0);

      if (startTime.isEqual(currentStart) || startTime.isEqual(currentEnd)) {
        return false;
      } else if (startTime.isAfter(currentStart) && startTime.isBefore(currentEnd)) {
        return false;
      } else if (endTime.isAfter(currentStart) && endTime.isBefore(currentEnd)) {
        return false;
      } else if (currentStart.isAfter(startTime) && currentStart.isBefore(endTime)) {
        return false;
      }
    }
    return true;
  }

  protected LocalDateTime getEnteredDate(String text) {
    LocalTime time = LocalTime.parse(text, hoursMinutesFormatter);
    return LocalDateTime.of(date.getValue(), time);
  }

  protected SimpleStringProperty createTimeStringProperty(LocalDateTime dateTime) {
    SimpleStringProperty retval = new SimpleStringProperty();
    if (dateTime != null) {
      retval.set(fullDateTimeFormatter.format(dateTime));
    }
    return retval;
  }

  public GridPane getRoot() {
    return root;
  }

  public Task getTask() {
    return task.get();
  }

  public SimpleObjectProperty<Task> taskProperty() {
    return task;
  }

  public void setTask(Task task) {
    this.task.set(task);
  }

  @FXML
  public void onEdit() {
    WorkUnit selectedItem = workUnitTable.getSelectionModel().getSelectedItem();
    LocalDateTime endTime = getEnteredDate(end.getText());
    LocalDateTime startTime = getEnteredDate(start.getText());
    store.executeCustomRunnable(() -> {
      PersistentWork.wrap(() -> {
        WorkUnit reload = PersistentWork.reload(selectedItem);
        reload.setStart(startTime);
        reload.setEnd(endTime);
      });
      controller.getJavaFXExecutor().submit(() -> reload(selectedItem.getTask()));
    });
  }

  @FXML
  public void onDelete() {
    WorkUnit selectedItem = workUnitTable.getSelectionModel().getSelectedItem();
    store.executeCustomRunnable(() -> {
      PersistentWork.run(em -> {
        WorkUnit reload = PersistentWork.reload(selectedItem);
        em.remove(reload);
      });
      controller.getJavaFXExecutor().submit(() -> reload(task.get()));
    });
  }

  @FXML
  public void onCreateNew() {
    Task currentTask = this.task.get();
    LocalDateTime endTime = getEnteredDate(end.getText());
    LocalDateTime startTime = getEnteredDate(start.getText());

    store.executeCustomRunnable(() -> {
      PersistentWork.wrap(() -> {
        Task reload = PersistentWork.reload(currentTask);

        WorkUnit workUnit = new WorkUnit(reload);
        workUnit.setStart(startTime);
        workUnit.setEnd(endTime);

        controller.getJavaFXExecutor().submit(() -> reload(currentTask));
      });
    });
  }
}
