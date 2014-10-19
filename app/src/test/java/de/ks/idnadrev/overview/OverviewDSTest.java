package de.ks.idnadrev.overview;

import de.ks.LauncherRunner;
import de.ks.fxcontrols.weekview.WeekHelper;
import de.ks.idnadrev.entity.Cleanup;
import de.ks.idnadrev.entity.Task;
import de.ks.persistence.PersistentWork;
import de.ks.scheduler.Schedule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(LauncherRunner.class)
public class OverviewDSTest {

  public static final String PROPOSED_THIS_WEEK_DAY = "proposedThisWeekDay";
  public static final String PROPOSED_THIS_WEEK = "proposedThisWeek";
  public static final String SCHEDULED_TODAY_NOON = "scheduledTodayNoon";
  public static final String SCHEDULED_TODAY = "scheduledToday";
  @Inject
  Cleanup cleanup;

  @Before
  public void setUp() throws Exception {
    cleanup.cleanup();
    PersistentWork.run(em -> {
      createTestData(em);
    });
  }

  protected void createTestData(EntityManager em) {
    em.persist(new Task("notScheduled"));

    Schedule schedule = new Schedule();
    schedule.setScheduledDate(LocalDate.now());
    em.persist(new Task(SCHEDULED_TODAY).setSchedule(schedule));
    em.persist(new Task("scheduledTodayButDone").setSchedule(schedule).setFinished(true));

    schedule = new Schedule();
    schedule.setScheduledDate(LocalDate.now());
    schedule.setScheduledTime(LocalTime.of(12, 42));
    em.persist(new Task(SCHEDULED_TODAY_NOON).setSchedule(schedule));

    schedule = new Schedule();
    schedule.setProposedWeek(new WeekHelper().getWeek(LocalDate.now()));
    schedule.setProposedYear(LocalDate.now().getYear());
    em.persist(new Task(PROPOSED_THIS_WEEK).setSchedule(schedule));
    em.persist(new Task("proposedThisWeekButDone").setSchedule(schedule).setFinished(true));

    schedule = new Schedule();
    schedule.setProposedWeek(new WeekHelper().getWeek(LocalDate.now()));
    schedule.setProposedYear(LocalDate.now().getYear());
    schedule.setProposedWeekDay(LocalDate.now().getDayOfWeek());
    em.persist(new Task(PROPOSED_THIS_WEEK_DAY).setSchedule(schedule));

    schedule = new Schedule();
    schedule.setProposedWeek(new WeekHelper().getWeek(LocalDate.now()));
    schedule.setProposedYear(LocalDate.now().getYear());
    schedule.setProposedWeekDay(LocalDate.now().plusDays(1).getDayOfWeek());
    em.persist(new Task("proposedOtherWeekDay").setSchedule(schedule));
  }

  @Test
  public void testProposedTasks() throws Exception {
    OverviewDS datasource = new OverviewDS();

    PersistentWork.run(em -> {
      List<Task> proposedTasks = datasource.getProposedTasks(em, LocalDate.now());
      assertEquals(2, proposedTasks.size());
      Set<String> tasknames = proposedTasks.stream().map(t -> t.getName()).collect(Collectors.toSet());

      assertTrue(tasknames.contains("proposedThisWeek"));
      assertTrue(tasknames.contains("proposedThisWeekDay"));
    });
  }

  @Test
  public void testScheduledTasks() throws Exception {
    OverviewDS datasource = new OverviewDS();

    PersistentWork.run(em -> {
      List<Task> proposedTasks = datasource.getScheduledTasks(em, LocalDate.now());
      assertEquals(2, proposedTasks.size());
      Set<String> tasknames = proposedTasks.stream().map(t -> t.getName()).collect(Collectors.toSet());

      assertTrue(tasknames.contains("scheduledTodayNoon"));
      assertTrue(tasknames.contains("scheduledToday"));
    });
  }
}