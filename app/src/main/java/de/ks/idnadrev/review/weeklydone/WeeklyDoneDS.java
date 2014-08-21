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

import de.ks.datasource.ListDataSource;
import de.ks.fxcontrols.weekview.WeekHelper;
import de.ks.fxcontrols.weekview.WeekViewAppointment;
import de.ks.idnadrev.entity.WorkUnit;
import de.ks.persistence.PersistentWork;
import de.ks.reflection.PropertyPath;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WeeklyDoneDS implements ListDataSource<WeekViewAppointment> {
  protected final WeekHelper helper = new WeekHelper();
  protected volatile LocalDateTime beginDate = LocalDateTime.of(helper.getFirstDayOfWeek(LocalDate.now()), LocalTime.of(0, 0));
  protected volatile LocalDateTime endDate = LocalDateTime.of(helper.getLastDayOfWeek(LocalDate.now()), LocalTime.of(23, 59));

  protected final List<WeekViewAppointment> resolvedAppointments = new CopyOnWriteArrayList<>();

  @Override
  public synchronized List<WeekViewAppointment> loadModel(Consumer<List<WeekViewAppointment>> furtherProcessing) {
    List<WorkUnit> workUnits = PersistentWork.from(WorkUnit.class, (root, query, builder) -> {
      Path start = root.get(PropertyPath.property(WorkUnit.class, unit -> unit.getStart()));
      Path end = root.get(PropertyPath.property(WorkUnit.class, unit -> unit.getEnd()));

      @SuppressWarnings("unchecked") Predicate greaterThan = builder.greaterThan(start, beginDate);
      @SuppressWarnings("unchecked") Predicate lessThan = builder.lessThan(end, endDate);
      Predicate workUnitEnded = builder.isNotNull(end);

      query.where(greaterThan, lessThan, workUnitEnded);
    }, unit -> unit.getTask().getName());

    List<WeekViewAppointment> appointments = workUnits.stream().map(unit -> {
      Duration duration = Duration.between(unit.getStart(), unit.getEnd());
      WeekViewAppointment appointment = new WeekViewAppointment(unit.getTask().getName(), unit.getStart(), duration, btn -> btn.getText());
      appointment.setNewTimePossiblePredicate(ldt -> false);
      return appointment;
    }).collect(Collectors.toList());
    resolvedAppointments.clear();
    resolvedAppointments.addAll(appointments);
    return appointments;
  }

  @Override
  public void saveModel(List<WeekViewAppointment> model, Consumer<List<WeekViewAppointment>> beforeSaving) {
//
  }
}
