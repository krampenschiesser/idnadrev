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

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.fxcontrols.weekview.WeekHelper;
import de.ks.fxcontrols.weekview.WeekViewAppointment;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.WorkUnit;
import de.ks.util.FXPlatform;
import javafx.collections.ObservableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static de.ks.JunitMatchers.withRetry;
import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class WeeklyDoneTest extends ActivityTest {

  private LocalDateTime weekStart;
  private WeeklyDone weeklyDone;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return WeeklyDoneActivity.class;
  }

  @Override
  protected void createTestData(EntityManager em) {
    Task finished = new Task("finished");
    weekStart = LocalDateTime.of(new WeekHelper().getFirstDayOfWeek(LocalDate.now().minusWeeks(1)), LocalTime.of(12, 0));
    finished.setFinishTime(weekStart);

    em.persist(finished);

    Task worker = new Task("worker");
    em.persist(worker);

    LocalDateTime start = weekStart;
    for (int i = 0; i < 3; i++) {
      WorkUnit workUnit = new WorkUnit(worker);
      workUnit.setStart(start.plusDays(i));
      workUnit.setEnd(start.plusDays(i).plusHours(1));
      em.persist(workUnit);
    }
    worker.setFinishTime(start.plusDays(2).plusHours(1));
  }

  @Before
  public void setUp() throws Exception {
    weeklyDone = activityController.getControllerInstance(WeeklyDone.class);
  }

  @Test
  public void testShowWeek() throws Exception {
    int week = new WeekHelper().getWeek(weekStart.toLocalDate());
    FXPlatform.invokeLater(() -> weeklyDone.weekView.weekOfYearProperty().set(week));
    withRetry(() -> !weeklyDone.weekView.getEntries().isEmpty());
    activityController.waitForDataSource();
    FXPlatform.waitForFX();

    ObservableList<WeekViewAppointment<Task>> entries = weeklyDone.weekView.getEntries();
    assertEquals(4, entries.size());
    WeekViewAppointment first = entries.get(0);
    assertEquals("finished", first.getTitle());
  }
}