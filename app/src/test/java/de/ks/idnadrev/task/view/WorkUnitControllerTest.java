package de.ks.idnadrev.task.view;

import de.ks.flatadocdb.session.Session;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.WorkUnit;
import de.ks.standbein.Condition;
import de.ks.standbein.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.activity.ActivityCfg;
import de.ks.standbein.validation.ValidationRegistry;
import de.ks.util.FXPlatform;
import javafx.collections.ObservableList;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

public class WorkUnitControllerTest extends ActivityTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IntegrationTestModule());

  private LocalDateTime start1;
  private LocalDateTime end1;
  private LocalDateTime start2;
  private LocalDateTime end2;
  private WorkUnitController controller;

  @Inject
  ValidationRegistry validation;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return ViewTasksActvity.class;
  }

  @Override
  protected void createTestData(Session session) {

    Task task = new Task("task");

    LocalDateTime time = LocalDateTime.of(2014, 10, 1, 8, 0);
    start1 = time.minusDays(1);
    end1 = start1.plusHours(1);

    start2 = time.minusHours(1);
    end2 = time.minusMinutes(1);

    WorkUnit workUnit1 = new WorkUnit(task).setStart(start1).setEnd(end1);
    WorkUnit workUnit2 = new WorkUnit(task).setStart(start2).setEnd(end2);

    task.getWorkUnits().add(workUnit1);
    task.getWorkUnits().add(workUnit2);

    session.persist(task);
  }

  @Before
  public void setUp() throws Exception {
    controller = activityController.getControllerInstance(WorkUnitController.class);
    FXPlatform.invokeLater(() -> controller.setTask(persistentWork.from(Task.class).get(0)));
  }

  @Test
  public void testWorkunitView() throws Exception {
    ObservableList<WorkUnit> items = controller.workUnitTable.getItems();
    assertEquals(2, items.size());
    assertEquals(start1, items.get(0).getStart());
    assertEquals(start2, items.get(1).getStart());
  }

  @Test
  public void testWorkUnitEditing() throws Exception {
    FXPlatform.invokeLater(() -> controller.workUnitTable.getSelectionModel().select(0));

    assertNotNull(controller.start.getText());
    assertThat(controller.start.getText(), Matchers.containsString(String.valueOf(start1.getHour())));
    assertNotNull(controller.end.getText());
    assertThat(controller.end.getText(), Matchers.containsString(String.valueOf(end1.getHour())));

    assertEquals(start1.toLocalDate(), controller.date.getValue());
    FXPlatform.invokeLater(() -> {
      controller.end.setText(controller.hoursMinutesFormatter.format(end1.plusMinutes(1)));
      controller.onEdit();
    });

    activityController.waitForDataSource();
    List<WorkUnit> from = persistentWork.from(WorkUnit.class);
    from.sort(Comparator.comparing(u -> u.getStart()));
    assertEquals(end1.plusMinutes(1), from.get(0).getEnd());
  }

  @Test
  public void testWorkUnitCreation() throws Exception {
    FXPlatform.invokeLater(() -> controller.workUnitTable.getSelectionModel().select(0));


    assertEquals(start1.toLocalDate(), controller.date.getValue());
    FXPlatform.invokeLater(() -> {
      controller.date.setValue(controller.date.getValue().minusDays(7));
      controller.onCreateNew();
    });

    activityController.waitForDataSource();
    FXPlatform.waitForFX();
    List<WorkUnit> from = persistentWork.from(WorkUnit.class);
    assertEquals(3, from.size());
  }

  @Test
  public void testValidation() throws Exception {
    FXPlatform.invokeLater(() -> controller.workUnitTable.getSelectionModel().select(0));

    FXPlatform.invokeLater(() -> controller.start.setText("bla"));
    FXPlatform.waitForFX();
    Condition.waitFor1s("should be disabled ", () -> controller.edit.isDisabled());

    FXPlatform.invokeLater(() -> {
      controller.start.setText("11:30");
      controller.end.setText("blubb");
    });
    FXPlatform.waitForFX();
    assertTrue(controller.edit.isDisabled());

    FXPlatform.invokeLater(() -> {
      controller.start.setText("11:30");
      controller.end.setText("11:25");
    });
    FXPlatform.waitForFX();
    assertTrue(controller.edit.isDisabled());

    FXPlatform.invokeLater(() -> {
      controller.start.setText("11:30");
      controller.end.setText("11:35");
    });
    FXPlatform.waitForFX();

    assertFalse(controller.edit.isDisabled());
  }

  @Test
  public void testNewStartTimePossible() throws Exception {
    WorkUnit workunit1 = new WorkUnit(null).setStart(LocalDateTime.of(2014, 10, 1, 12, 00, 13, 10)).setEnd(LocalDateTime.of(2014, 10, 1, 12, 13));
    WorkUnit workunit2 = new WorkUnit(null).setStart(LocalDateTime.of(2014, 10, 1, 13, 00)).setEnd(LocalDateTime.of(2014, 10, 1, 14, 00));
    List<WorkUnit> workUnits = Arrays.asList(workunit1, workunit2);

    LocalDateTime time = LocalDateTime.of(2014, 10, 1, 12, 0);
    assertFalse(controller.checkNewStartTimePossible(time, time.plusMinutes(1), workUnits));
    time = LocalDateTime.of(2014, 10, 1, 12, 13);
    assertFalse(controller.checkNewStartTimePossible(time, time.plusMinutes(1), workUnits));
    time = LocalDateTime.of(2014, 10, 1, 13, 13);
    assertFalse(controller.checkNewStartTimePossible(time, time.plusMinutes(1), workUnits));

    time = LocalDateTime.of(2014, 10, 1, 12, 14);
    assertTrue(controller.checkNewStartTimePossible(time, time.plusMinutes(1), workUnits));

    time = LocalDateTime.of(2014, 10, 1, 12, 59);
    assertFalse(controller.checkNewStartTimePossible(time, time.plusHours(2), workUnits));
  }

  @Test
  public void testWorkUnitDelete() throws Exception {
    assertTrue(controller.delete.isDisabled());

    FXPlatform.invokeLater(() -> controller.workUnitTable.getSelectionModel().select(0));
    assertFalse(controller.delete.isDisabled());

    FXPlatform.invokeLater(() -> controller.onDelete());
    activityController.waitForDataSource();

    List<WorkUnit> workUnits = persistentWork.from(WorkUnit.class);
    assertEquals(1, workUnits.size());

    Condition.waitFor1s(() -> controller.workUnitTable.getItems(), Matchers.hasSize(1));
  }
}