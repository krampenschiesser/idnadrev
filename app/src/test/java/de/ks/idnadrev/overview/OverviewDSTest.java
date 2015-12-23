package de.ks.idnadrev.overview;

import de.ks.flatadocdb.session.Session;
import de.ks.flatjsondb.PersistentWork;
import de.ks.fxcontrols.weekview.WeekHelper;
import de.ks.idnadrev.entity.Schedule;
import de.ks.idnadrev.entity.Task;
import de.ks.standbein.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class OverviewDSTest {

  public static final String PROPOSED_THIS_WEEK_DAY = "proposedThisWeekDay";
  public static final String PROPOSED_THIS_WEEK = "proposedThisWeek";
  public static final String SCHEDULED_TODAY_NOON = "scheduledTodayNoon";
  public static final String SCHEDULED_TODAY = "scheduledToday";
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IntegrationTestModule());

  @Inject
  PersistentWork persistentWork;

  @Before
  public void setUp() throws Exception {
    persistentWork.run(em -> {
      createTestData(em);
    });
  }

  protected void createTestData(Session session) {
    session.persist(new Task("notScheduled"));

    Schedule schedule = new Schedule();
    schedule.setScheduledDate(LocalDate.now());
    session.persist(new Task(SCHEDULED_TODAY).setSchedule(schedule));
    session.persist(new Task("scheduledTodayButDone").setSchedule(schedule).setFinished(true));

    schedule = new Schedule();
    schedule.setScheduledDate(LocalDate.now());
    schedule.setScheduledTime(LocalTime.of(12, 42));
    session.persist(new Task(SCHEDULED_TODAY_NOON).setSchedule(schedule));

    schedule = new Schedule();
    schedule.setProposedWeek(new WeekHelper().getWeek(LocalDate.now()));
    schedule.setProposedYear(LocalDate.now().getYear());
    session.persist(new Task(PROPOSED_THIS_WEEK).setSchedule(schedule));
    session.persist(new Task("proposedThisWeekButDone").setSchedule(schedule).setFinished(true));

    schedule = new Schedule();
    schedule.setProposedWeek(new WeekHelper().getWeek(LocalDate.now()));
    schedule.setProposedYear(LocalDate.now().getYear());
    schedule.setProposedWeekDay(LocalDate.now().getDayOfWeek());
    session.persist(new Task(PROPOSED_THIS_WEEK_DAY).setSchedule(schedule));

    schedule = new Schedule();
    schedule.setProposedWeek(new WeekHelper().getWeek(LocalDate.now()));
    schedule.setProposedYear(LocalDate.now().getYear());
    schedule.setProposedWeekDay(LocalDate.now().plusDays(1).getDayOfWeek());
    session.persist(new Task("proposedOtherWeekDay").setSchedule(schedule));
  }

  @Test
  public void testProposedTasks() throws Exception {
    OverviewDS datasource = new OverviewDS();

    persistentWork.run(sesion -> {
      List<Task> proposedTasks = datasource.getProposedTasks(sesion, LocalDate.now());
      assertEquals(2, proposedTasks.size());
      Set<String> tasknames = proposedTasks.stream().map(t -> t.getName()).collect(Collectors.toSet());

      assertTrue(tasknames.contains("proposedThisWeek"));
      assertTrue(tasknames.contains("proposedThisWeekDay"));
    });
  }

  @Test
  public void testScheduledTasks() throws Exception {
    OverviewDS datasource = new OverviewDS();

    persistentWork.run(sesion -> {
      List<Task> proposedTasks = datasource.getScheduledTasks(sesion, LocalDate.now());
      assertEquals(2, proposedTasks.size());
      Set<String> tasknames = proposedTasks.stream().map(t -> t.getName()).collect(Collectors.toSet());

      assertTrue(tasknames.contains("scheduledTodayNoon"));
      assertTrue(tasknames.contains("scheduledToday"));
    });
  }
}