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

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.fxcontrols.weekview.WeekHelper;
import de.ks.fxcontrols.weekview.WeekViewAppointment;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.Task;
import de.ks.persistence.PersistentWork;
import de.ks.scheduler.Schedule;
import de.ks.util.FXPlatform;
import javafx.collections.ObservableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(LauncherRunner.class)
public class PlanWeekActivityTest extends ActivityTest {

  private PlanWeek planWeek;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return PlanWeekActivity.class;
  }

  @Override
  protected void createTestData(EntityManager em) {

    LocalDate firstDayOfWeek = new WeekHelper().getFirstDayOfWeek(LocalDate.now());

    Task proposed = new Task("proposed");
    Schedule schedule = new Schedule();
    schedule.setProposedWeek(new WeekHelper().getWeek(LocalDate.now()));
    schedule.setProposedWeekDay(DayOfWeek.THURSDAY);
    proposed.setSchedule(schedule);
    em.persist(proposed);

    Task scheduled = new Task("scheduled");
    schedule = new Schedule();
    schedule.setScheduledDate(firstDayOfWeek.plusDays(1));
    schedule.setScheduledTime(LocalTime.of(12, 0));
    scheduled.setSchedule(schedule);
    em.persist(scheduled);
  }

  @Before
  public void setUp() throws Exception {
    planWeek = activityController.getControllerInstance(PlanWeek.class);
  }

  @Test
  public void testResolveProposedAndScheduled() throws Exception {
    assertEquals(2, planWeek.viewController.getTasks().size());
    assertEquals(planWeek.weekView.getEntries().toString(), 2, planWeek.weekView.getEntries().size());
  }

  @Test
  public void testCreateAppointment() throws Exception {
    LocalDate firstDayOfWeek = new WeekHelper().getFirstDayOfWeek(LocalDate.now());
    assertEquals(DayOfWeek.MONDAY, firstDayOfWeek.getDayOfWeek());//sanity on sunday morning

    WeekViewAppointment<Task> appointment = planWeek.createAppointment(PersistentWork.forName(Task.class, "proposed"));
    assertEquals(firstDayOfWeek.plusDays(3), appointment.getStartDate());
    assertNull(appointment.getStartTime());

    appointment = planWeek.createAppointment(PersistentWork.forName(Task.class, "scheduled"));
    assertEquals(firstDayOfWeek.plusDays(1), appointment.getStartDate());
    assertEquals(LocalTime.of(12, 0), appointment.getStartTime());
  }

  @Test
  public void testReplan() throws Exception {
    WeekViewAppointment<Task> proposed = planWeek.createAppointment(PersistentWork.forName(Task.class, "proposed"));
    WeekViewAppointment<Task> scheduled = planWeek.createAppointment(PersistentWork.forName(Task.class, "scheduled"));

    planWeek.changeSchedule(proposed, LocalDate.now(), LocalTime.of(13, 0));
    planWeek.changeSchedule(scheduled, LocalDate.now(), null);

    PersistentWork.wrap(() -> {
      Schedule schedule = PersistentWork.forName(Task.class, "proposed").getSchedule();
      assertEquals(0, schedule.getProposedWeek());
      assertEquals(0, schedule.getProposedYear());
      assertNull(schedule.getProposedWeekDay());

      assertEquals(LocalDate.now(), schedule.getScheduledDate());
      assertEquals(LocalTime.of(13, 0), schedule.getScheduledTime());
    });
    PersistentWork.wrap(() -> {
      Schedule schedule = PersistentWork.forName(Task.class, "scheduled").getSchedule();
      WeekHelper weekHelper = new WeekHelper();
      int week = weekHelper.getWeek(LocalDate.now());
      DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
      assertEquals(week, schedule.getProposedWeek());
      assertEquals(Year.now().getValue(), schedule.getProposedYear());
      assertEquals(dayOfWeek, schedule.getProposedWeekDay());

      assertNull(schedule.getScheduledDate());
      assertNull(schedule.getScheduledTime());
    });
  }

  @Test
  public void testStartDragOutOfRange() throws Exception {
    FXPlatform.invokeLater(() -> planWeek.weekView.weekOfYearProperty().set(planWeek.weekView.getWeekOfYear() + 2));
    activityController.waitForDataSource();
    WeekViewAppointment<Task> appointment = planWeek.createAppointment(PersistentWork.forName(Task.class, "proposed"));


    ObservableList<WeekViewAppointment<Task>> entries = planWeek.weekView.getEntries();
    assertEquals(0, entries.size());
    FXPlatform.invokeLater(() -> planWeek.weekView.startAppointmentDrag(appointment));
    assertEquals(1, entries.size());
    assertEquals(planWeek.weekView.getFirstDayOfWeek(), entries.get(0).getStartDate());
  }
}