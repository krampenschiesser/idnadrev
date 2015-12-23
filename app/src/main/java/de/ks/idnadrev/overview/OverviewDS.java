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
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Schedule;
import de.ks.idnadrev.entity.Task;
import de.ks.standbein.datasource.DataSource;
import de.ks.standbein.reflection.PropertyPath;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class OverviewDS implements DataSource<OverviewModel> {
  protected static final String KEY_SCHEDULE = PropertyPath.property(Task.class, t -> t.getSchedule());
  protected static final String KEY_PROPOSEDWEEK = PropertyPath.property(Schedule.class, t -> t.getProposedWeek());
  protected static final String KEY_PROPOSEDYEAR = PropertyPath.property(Schedule.class, t -> t.getProposedYear());
  protected static final String KEY_SCHEDULEDDATE = PropertyPath.property(Schedule.class, t -> t.getScheduledDate());

  @Inject
  PersistentWork persistentWork;

  @Override
  public OverviewModel loadModel(Consumer<OverviewModel> furtherProcessing) {
    return persistentWork.read(em -> {
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
//    CriteriaBuilder builder = em.getCriteriaBuilder();
//    CriteriaQuery<Task> query = builder.createQuery(Task.class);
//    Root<Task> root = query.from(Task.class);
//    Path<Schedule> schedule = root.get(KEY_SCHEDULE);
//
//    WeekHelper helper = new WeekHelper();
//    int week = helper.getWeek(now);
//    int year = now.getYear();
//
//    query.select(root);
//    Predicate correctYear = builder.equal(schedule.get(KEY_PROPOSEDYEAR), year);
//    Predicate correctWeek = builder.equal(schedule.get(KEY_PROPOSEDWEEK), week);
//    query.where(correctWeek, correctYear);
//
//    List<Task> results = em.createQuery(query).getResultList();
//    List<Task> retval = results.stream().filter(r -> !r.isFinished()).filter(t -> {
//      DayOfWeek proposedWeekDay = t.getSchedule().getProposedWeekDay();
//      if (proposedWeekDay != null) {
//        return proposedWeekDay.equals(now.getDayOfWeek());
//      }
//      return true;
//    }).collect(Collectors.toList());
//
//    Collections.sort(retval, Comparator.comparing(t -> t.getRemainingTime()));
//    // FIXME: 12/15/15
    return Collections.emptyList();
  }

  protected List<Task> getScheduledTasks(Session sesion, LocalDate now) {
//    CriteriaBuilder builder = em.getCriteriaBuilder();
//    CriteriaQuery<Task> query = builder.createQuery(Task.class);
//    Root<Task> root = query.from(Task.class);
//    Path<Schedule> schedule = root.get(KEY_SCHEDULE);
//
//    WeekHelper helper = new WeekHelper();
//
//    query.select(root);
//    Predicate correctDate = builder.equal(schedule.get(KEY_SCHEDULEDDATE), now);
//    query.where(correctDate);
//
//    List<Task> results = em.createQuery(query).getResultList();
//    List<Task> retval = results.stream().filter(r -> !r.isFinished()).collect(Collectors.toList());
//
//    return retval;
    //FIXME
    return Collections.emptyList();
  }

  @Override
  public void saveModel(OverviewModel model, Consumer<OverviewModel> beforeSaving) {

  }
}
