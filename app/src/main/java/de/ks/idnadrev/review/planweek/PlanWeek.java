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
package de.ks.idnadrev.review.planweek;

import de.ks.BaseController;
import de.ks.activity.ActivityHint;
import de.ks.fxcontrols.weekview.AppointmentResolver;
import de.ks.fxcontrols.weekview.WeekHelper;
import de.ks.fxcontrols.weekview.WeekView;
import de.ks.fxcontrols.weekview.WeekViewAppointment;
import de.ks.i18n.Localized;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.task.create.CreateTaskActivity;
import de.ks.idnadrev.task.finish.FinishTaskActivity;
import de.ks.idnadrev.task.view.ViewTasksMaster;
import de.ks.persistence.PersistentWork;
import de.ks.scheduler.Schedule;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PlanWeek extends BaseController<List<Task>> implements AppointmentResolver<Task> {
  private static final Logger log = LoggerFactory.getLogger(PlanWeek.class);
  @FXML
  protected ViewTasksMaster viewController;
  @FXML
  protected StackPane weekViewContainer;

  @FXML
  protected Button edit;
  @FXML
  protected Button finish;
  @FXML
  protected Button delete;

  protected WeekView<Task> weekView;
  protected final List<Task> tasks = new LinkedList<>();
  protected final SimpleBooleanProperty disable = new SimpleBooleanProperty(true);

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    weekView = new WeekView<>(Localized.get("today"));
    weekView.setPrefSize(300, 300);
    weekViewContainer.getChildren().add(weekView);
    weekView.setAppointmentResolver(this);
    viewController.getTasksView().getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
      if (n != null) {
        Task task = n.getValue();
        WeekViewAppointment<Task> appointment = createAppointment(task);
        appointment.getControl().setVisible(false);
        if (!weekView.getEntries().contains(appointment)) {
          weekView.getEntries().add(appointment);
        }
      }
    });
    viewController.getTasksView().setOnMouseClicked(e -> {
      onEdit();
    });

    viewController.getTasksView().setOnDragDetected(e -> {
      TreeItem<Task> selectedItem = viewController.getTasksView().getSelectionModel().getSelectedItem();
      if (selectedItem == null) {
        return;
      }
      Task task = selectedItem.getValue();

      WeekViewAppointment<Task> appointment = createAppointment(task);

      weekView.startAppointmentDrag(appointment);
      log.debug("Detected drag gesture, selected item={}", task.getName());
      e.consume();
    });

    viewController.getTasksView().getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
      if (n != null && n.getValue() != null && n.getValue().getId() >= 0) {
        disable.set(false);
      } else {
        disable.set(true);
      }
    });
    finish.disableProperty().bind(disable);
    edit.disableProperty().bind(disable);
    delete.disableProperty().bind(disable);
  }

  protected WeekViewAppointment<Task> createAppointment(Task task) {
    WeekViewAppointment<Task> appointment = new WeekViewAppointment<>(task.getName(), weekView.getFirstDayOfWeek(), task.getEstimatedTime());
    Schedule taskSchedule = task.getSchedule();
    if (taskSchedule != null) {
      if (taskSchedule.getDuration() != null) {
        appointment.setDuration(taskSchedule.getDuration());
      }
      if (taskSchedule.getScheduledDate() != null) {
        appointment.setStart(taskSchedule.getScheduledDate(), taskSchedule.getScheduledTime());
      } else if (taskSchedule.getProposedWeek() != 0) {
        LocalDate start = new WeekHelper().getFirstDayOfWeek(taskSchedule.getProposedYear(), taskSchedule.getProposedWeek());
        if (taskSchedule.getProposedWeekDay() != null) {
          start = start.plusDays(taskSchedule.getProposedWeekDay().getValue() - 1);
        }
        appointment.setStart(start, null);
      }
    }
    appointment.setUserData(task);
    appointment.setChangeStartCallback((date, time) -> {
      controller.getExecutorService().submit(() -> changeSchedule(appointment, date, time));
    });
    appointment.setNewTimePossiblePredicate((date, time) -> true);
    appointment.setEnhancer(btn -> {
      MenuItem item = new MenuItem(Localized.get("change.duration"));
      item.setOnAction(e -> changeDuration(appointment, task));
      ContextMenu menu = new ContextMenu(item);
      btn.setContextMenu(menu);
    });
    appointment.setAction((btn, t) -> {
      edit(() -> t);
    });
    return appointment;
  }

  protected void changeDuration(WeekViewAppointment<Task> appointment, Task task) {
    Optional<String> optional = Dialogs.create().title(Localized.get("enter.time.minutes")).showTextInput(String.valueOf(appointment.getDuration().toMinutes()));
    if (optional.isPresent()) {
      String input = optional.get();
      try {
        long duration = Long.parseLong(input);
        Duration minutes = Duration.ofMinutes(duration);
        appointment.setDuration(minutes);
        PersistentWork.wrap(() -> {
          Task reloaded = PersistentWork.reload(appointment.getUserData());
          reloaded.getSchedule().setDuration(minutes);
        });
        weekView.recreateEntries();
      } catch (NumberFormatException e) {
        log.warn("Could not parse input {}", input);
      }
    }
  }

  protected void changeSchedule(WeekViewAppointment<Task> appointment, LocalDate date, LocalTime time) {
    PersistentWork.wrap(() -> {
      Task reloaded = PersistentWork.reload(appointment.getUserData());
      Schedule schedule = reloaded.getSchedule();
      if (schedule == null) {
        schedule = new Schedule();
        reloaded.setSchedule(schedule);
      }
      if (time == null) {
        int week = new WeekHelper().getWeek(date);
        schedule.setProposedWeek(week);
        schedule.setProposedWeekDay(date.getDayOfWeek());
        schedule.setScheduledDate(null);
        schedule.setScheduledTime(null);
      } else {
        schedule.setScheduledDate(date);
        schedule.setScheduledTime(time);
        schedule.setProposedWeek(0);
        schedule.setProposedYear(0);
        schedule.setProposedWeekDay(null);
      }
      log.info("{} now starting at {} {}", reloaded.getName(), date, time);
    });
  }

  @Override
  public void resolve(LocalDate begin, LocalDate end, Consumer<List<WeekViewAppointment<Task>>> callback) {
    WeekHelper weekHelper = new WeekHelper();

    List<Task> reloaded = PersistentWork.read(em -> tasks.stream().map(t -> PersistentWork.reload(t)).collect(Collectors.toList()));

    List<Task> tasksWithScheduledDate = reloaded.stream().filter(t -> t.getSchedule() != null)//
      .filter(t -> t.getSchedule().getScheduledDate() != null)//
      .filter(t -> t.getSchedule().getScheduledDate().isAfter(begin.minusDays(1)))//
      .filter(t -> t.getSchedule().getScheduledDate().isBefore(end.plusDays(1)))//
      .collect(Collectors.toList());
    log.info("found {} tasks with scheduled date in range {} - {}", tasksWithScheduledDate.size(), begin, end);

    List<Task> tasksWithProposedDate = reloaded.stream().filter(t -> t.getSchedule() != null)//
      .filter(t -> t.getSchedule().getProposedWeek() != 0)//
      .filter(t -> weekHelper.getFirstDayOfWeek(t.getSchedule().getProposedYear(), t.getSchedule().getProposedWeek()).isAfter(begin.minusDays(1)))//
      .filter(t -> weekHelper.getLastDayOfWeek(t.getSchedule().getProposedYear(), t.getSchedule().getProposedWeek()).isBefore(end.plusDays(1)))//
      .collect(Collectors.toList());
    log.info("found {} tasks with prosoded date in range {} - {}", tasksWithProposedDate.size(), begin, end);

    HashSet<Task> all = new HashSet<>();
    all.addAll(tasksWithProposedDate);
    all.addAll(tasksWithScheduledDate);
    List<WeekViewAppointment<Task>> appointments = all.stream().map(t -> createAppointment(t)).collect(Collectors.toList());
    callback.accept(appointments);
  }

  @Override
  protected void onRefresh(List<Task> model) {
    this.tasks.clear();
    this.tasks.addAll(model);
    weekView.recreateEntries();
  }

  @FXML
  protected void onEdit() {
    TreeTableView<Task> tasksView = viewController.getTasksView();
    Supplier<Task> supplier = () -> tasksView.getSelectionModel().getSelectedItem().getValue();
    edit(supplier);
  }

  protected void edit(Supplier<Task> supplier) {
    ActivityHint hint = new ActivityHint(CreateTaskActivity.class);
    hint.setReturnToActivity(controller.getCurrentActivityId());

    hint.setReturnToDatasourceHint(supplier);
    hint.setDataSourceHint(supplier);

    controller.startOrResume(hint);
  }

  @FXML
  protected void onFinish() {
    TreeTableView<Task> tasksView = viewController.getTasksView();
    ActivityHint activityHint = new ActivityHint(FinishTaskActivity.class, controller.getCurrentActivityId());
    activityHint.setDataSourceHint(() -> tasksView.getSelectionModel().getSelectedItem().getValue());

    controller.startOrResume(activityHint);
  }

  @FXML
  protected void onDelete() {
    TreeTableView<Task> tasksView = viewController.getTasksView();
    PersistentWork.run(em -> {
      Task task = tasksView.getSelectionModel().getSelectedItem().getValue();
      em.remove(PersistentWork.reload(task));
    });
    controller.reload();
  }
}
