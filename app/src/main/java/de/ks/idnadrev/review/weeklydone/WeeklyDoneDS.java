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

import de.ks.activity.ActivityController;
import de.ks.datasource.ListDataSource;
import de.ks.fxcontrols.weekview.WeekHelper;
import de.ks.fxcontrols.weekview.WeekViewAppointment;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.WorkUnit;
import de.ks.imagecache.Images;
import de.ks.persistence.PersistentWork;
import de.ks.reflection.PropertyPath;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WeeklyDoneDS implements ListDataSource<WeekViewAppointment<Task>> {
  private static final Logger log = LoggerFactory.getLogger(WeeklyDoneDS.class);
  public static final int DONE_IMG_HEIGHT = 15;
  protected final WeekHelper helper = new WeekHelper();
  protected volatile LocalDateTime beginDate = LocalDateTime.of(helper.getFirstDayOfWeek(LocalDate.now()), LocalTime.of(0, 0));
  protected volatile LocalDateTime endDate = LocalDateTime.of(helper.getLastDayOfWeek(LocalDate.now()), LocalTime.of(23, 59));

  protected final List<WeekViewAppointment<Task>> resolvedAppointments = new CopyOnWriteArrayList<>();

  @Inject
  ActivityController controller;

  @Override
  public synchronized List<WeekViewAppointment<Task>> loadModel(Consumer<List<WeekViewAppointment<Task>>> furtherProcessing) {
    WeeklyDoneAppointmentView doneView = controller.getControllerInstance(WeeklyDoneAppointmentView.class);

    List<WorkUnit> workUnits = PersistentWork.from(WorkUnit.class, (root, query, builder) -> {
      Path start = root.get(PropertyPath.property(WorkUnit.class, unit -> unit.getStart()));
      Path end = root.get(PropertyPath.property(WorkUnit.class, unit -> unit.getEnd()));

      @SuppressWarnings("unchecked") Predicate greaterThan = builder.greaterThan(start, beginDate);
      @SuppressWarnings("unchecked") Predicate lessThan = builder.lessThan(end, endDate);
      Predicate workUnitEnded = builder.isNotNull(end);

      query.where(greaterThan, lessThan, workUnitEnded);
    }, unit -> unit.getTask().getName());


    List<Task> finishedTasks = PersistentWork.from(Task.class, (root, query, builder) -> {
      Path finishTime = root.get(PropertyPath.property(Task.class, task -> task.getFinishTime()));
      Predicate finished = builder.isNotNull(finishTime);
      @SuppressWarnings("unchecked") Predicate greaterThan = builder.greaterThan(finishTime, beginDate);
      @SuppressWarnings("unchecked") Predicate lessThan = builder.lessThan(finishTime, endDate);
      query.where(greaterThan, lessThan, finished);
    }, null);
    log.debug("Found {} finished tasks and {} workunits for the given range {} - {}", finishedTasks.size(), workUnits.size(), beginDate, endDate);

    List<WeekViewAppointment<Task>> appointments = workUnits.stream().map(unit -> {
      Duration duration = Duration.between(unit.getStart(), unit.getEnd());
      WeekViewAppointment<Task> appointment = new WeekViewAppointment<>(unit.getTask().getName(), unit.getStart(), duration);
      appointment.setNewTimePossiblePredicate(ldt -> false);
      appointment.setUserData(unit.getTask());
      appointment.setAction((btn, task) -> doneView.appointment.set(appointment));
      return appointment;
    }).collect(Collectors.toList());


    List<WeekViewAppointment<Task>> finished = finishedTasks.stream().map(task -> {
      Optional<WeekViewAppointment<Task>> first = appointments.stream().filter(a -> a.contains(task.getFinishTime())).findFirst();
      if (first.isPresent()) {
        WeekViewAppointment<Task> existing = first.get();
        String doneImagePath = getClass().getResource("done.png").toExternalForm();
        Image image = Images.get(doneImagePath);
        ImageView view = new ImageView(image);
        existing.setEnhancer(btn -> {
          view.setFitHeight(DONE_IMG_HEIGHT);
          view.setPreserveRatio(true);
          view.setSmooth(true);
          btn.setGraphic(view);
          btn.setContentDisplay(ContentDisplay.RIGHT);
        });
        return null;
      } else {
        WeekViewAppointment<Task> appointment = new WeekViewAppointment<>(task.getName(), task.getFinishTime().minusMinutes(15), Duration.ofMinutes(15));
        appointment.setNewTimePossiblePredicate(ldt -> false);
        appointment.setUserData(task);
        appointment.setAction((btn, t) -> doneView.appointment.set(appointment));
        return appointment;
      }
    }).filter(n -> n != null).collect(Collectors.toList());
    resolvedAppointments.clear();
    resolvedAppointments.addAll(appointments);
    resolvedAppointments.addAll(finished);

//    resolvedAppointments.forEach(appointment -> appointment.set);
    return resolvedAppointments;
  }

  @Override
  public void saveModel(List<WeekViewAppointment<Task>> model, Consumer<List<WeekViewAppointment<Task>>> beforeSaving) {
//
  }
}
