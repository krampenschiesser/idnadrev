/**
 * Copyright [2014] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.idnadrev.overview;

import de.ks.flatadocdb.session.Session;
import de.ks.flatjsondb.PersistentWork;
import de.ks.fxcontrols.weekview.WeekHelper;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Schedule;
import de.ks.idnadrev.entity.Task;
import de.ks.standbein.datasource.DataSource;
import de.ks.standbein.reflection.PropertyPath;

import javax.inject.Inject;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class OverviewDS implements DataSource<OverviewModel> {
  protected static final String KEY_SCHEDULE = PropertyPath.property(Task.class, t -> t.getSchedule());
  protected static final String KEY_PROPOSEDWEEK = PropertyPath.property(Schedule.class, t -> t.getProposedWeek());
  protected static final String KEY_PROPOSEDYEAR = PropertyPath.property(Schedule.class, t -> t.getProposedYear());
  protected static final String KEY_SCHEDULEDDATE = PropertyPath.property(Schedule.class, t -> t.getScheduledDate());

  @Inject
  PersistentWork persistentWork;

  @Override
  public OverviewModel loadModel(Consumer<OverviewModel> furtherProcessing) {
    return persistentWork.read(sesion -> {
      OverviewModel model = new OverviewModel();
      List<Context> contexts = persistentWork.from(Context.class);
      model.getContexts().addAll(contexts);

      List<Task> scheduled = getScheduledTasks(sesion, LocalDate.now());
      model.getScheduledTasks().addAll(scheduled);

      List<Task> proposedTasks = getProposedTasks(sesion, LocalDate.now());
      model.getProposedTasks().addAll(proposedTasks);
      return model;
    });
  }

  protected List<Task> getProposedTasks(Session sesion, LocalDate now) {
    WeekHelper helper = new WeekHelper();
    final Integer week = helper.getWeek(now);
    final Integer year = now.getYear();

    Session.MultiQueyBuilder<Task> b = sesion.multiQuery(Task.class);
    b.query(Task.proposedWeek(), t -> t != null && t.intValue() == week);
    b.query(Task.proposedYear(), t -> t != null && t.intValue() == year);
    Set<Task> results = b.find();
    List<Task> retval = results.stream().filter(r -> !r.isFinished()).filter(t -> {
      DayOfWeek proposedWeekDay = t.getSchedule().getProposedWeekDay();
      if (proposedWeekDay != null) {
        return proposedWeekDay.equals(now.getDayOfWeek());
      }
      return true;
    }).collect(Collectors.toList());

    Collections.sort(retval, Comparator.comparing(Task::getRemainingTime).thenComparing(Task::getName));
    return retval;
  }

  protected List<Task> getScheduledTasks(Session sesion, LocalDate now) {
    List<Task> retval = persistentWork.query(Task.class, Task.scheduledTime(),//
      t -> t != null && t.toLocalDate().equals(now))//
      .stream().filter(t -> !t.isFinished()).collect(Collectors.toList());
    return retval;
  }

  @Override
  public void saveModel(OverviewModel model, Consumer<OverviewModel> beforeSaving) {

  }
}
