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
package de.ks.idnadrev.review.weeklydone;

import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WeeklyDoneDS implements ListDataSource<WeekViewAppointment<Task>> {
  private static final Logger log = LoggerFactory.getLogger(WeeklyDoneDS.class);
  public static final int DONE_IMG_HEIGHT = 15;
  protected final WeekHelper helper = new WeekHelper();
  protected volatile LocalDateTime beginDate = LocalDateTime.of(helper.getFirstDayOfWeek(LocalDate.now()), LocalTime.of(0, 0));
  protected volatile LocalDateTime endDate = LocalDateTime.of(helper.getLastDayOfWeek(LocalDate.now()), LocalTime.of(23, 59));

  protected final List<WeekViewAppointment<Task>> resolvedAppointments = new LinkedList<>();

  @Inject
  ActivityController controller;

  @Override
  public synchronized List<WeekViewAppointment<Task>> loadModel(Consumer<List<WeekViewAppointment<Task>>> furtherProcessing) {

    List<WeekViewAppointment<Task>> appointments = getWorkUnits();
    List<WeekViewAppointment<Task>> finished = getFinshedTaskAppointments(appointments);
    resolvedAppointments.clear();
    resolvedAppointments.addAll(appointments);
    resolvedAppointments.addAll(finished);
    return resolvedAppointments;
  }

  protected List<WeekViewAppointment<Task>> getWorkUnits() {
    WeeklyDoneAppointmentView doneView = controller.getControllerInstance(WeeklyDoneAppointmentView.class);
    List<WorkUnit> workUnits = PersistentWork.from(WorkUnit.class, (root, query, builder) -> {
      Path start = root.get(PropertyPath.property(WorkUnit.class, unit -> unit.getStart()));
      Path end = root.get(PropertyPath.property(WorkUnit.class, unit -> unit.getEnd()));

      @SuppressWarnings("unchecked") Predicate greaterThan = builder.greaterThan(start, beginDate);
      @SuppressWarnings("unchecked") Predicate lessThan = builder.lessThan(end, endDate);
      Predicate workUnitEnded = builder.isNotNull(end);

      query.where(greaterThan, lessThan, workUnitEnded);
    }, unit -> unit.getTask().getName());
    log.debug("Found {} workunits for the given range {} - {}", workUnits.size(), beginDate, endDate);

    return workUnits.stream().map(unit -> {
      Duration duration = Duration.between(unit.getStart(), unit.getEnd());
      WeekViewAppointment<Task> appointment = new WeekViewAppointment<>(unit.getTask().getName(), unit.getStart(), duration);
      appointment.setNewTimePossiblePredicate((date, time) -> false);
      appointment.setUserData(unit.getTask());
      appointment.setAction((btn, task) -> doneView.appointment.set(appointment));
      return appointment;
    }).collect(Collectors.toList());
  }

  protected List<WeekViewAppointment<Task>> getFinshedTaskAppointments(List<WeekViewAppointment<Task>> appointments) {
    WeeklyDoneAppointmentView doneView = controller.getControllerInstance(WeeklyDoneAppointmentView.class);

    List<Task> finishedTasks = PersistentWork.from(Task.class, (root, query, builder) -> {
      Path finishTime = root.get(PropertyPath.property(Task.class, task -> task.getFinishTime()));
      Predicate finished = builder.isNotNull(finishTime);
      @SuppressWarnings("unchecked") Predicate greaterThan = builder.greaterThan(finishTime, beginDate);
      @SuppressWarnings("unchecked") Predicate lessThan = builder.lessThan(finishTime, endDate);
      query.where(greaterThan, lessThan, finished);
    }, null);
    log.debug("Found {} finished tasks for the given range {} - {}", finishedTasks.size(), beginDate, endDate);


    String doneImagePath = getClass().getResource("done.png").toExternalForm();
    Image image = Images.get(doneImagePath);
    List<WeekViewAppointment<Task>> finishedAppointments = finishedTasks.stream().map(task -> {
      ImageView view = new ImageView(image);
      Consumer<Button> enhanceForFinish = btn -> {
        view.setFitHeight(DONE_IMG_HEIGHT);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        btn.setGraphic(view);
        btn.setContentDisplay(ContentDisplay.RIGHT);
      };
      Optional<WeekViewAppointment<Task>> first = appointments.stream().filter(a -> a.getUserData().equals(task) && a.contains(task.getFinishTime())).findFirst();
      if (first.isPresent()) {
        WeekViewAppointment<Task> existing = first.get();
        existing.setEnhancer(enhanceForFinish);
        return null;
      } else {
        WeekViewAppointment<Task> appointment = new WeekViewAppointment<>(task.getName(), task.getFinishTime().minusMinutes(15), Duration.ofMinutes(15));
        appointment.setNewTimePossiblePredicate((date, time) -> false);
        appointment.setUserData(task);
        appointment.setEnhancer(enhanceForFinish);
        appointment.setAction((btn, t) -> doneView.appointment.set(appointment));
        return appointment;
      }
    }).filter(n -> n != null).collect(Collectors.toList());
    return finishedAppointments;
  }

  @Override
  public void saveModel(List<WeekViewAppointment<Task>> model, Consumer<List<WeekViewAppointment<Task>>> beforeSaving) {
    //
  }
}
