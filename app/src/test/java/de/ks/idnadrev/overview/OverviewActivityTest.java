package de.ks.idnadrev.overview;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.Thought;
import de.ks.persistence.PersistentWork;
import de.ks.util.FXPlatform;
import javafx.collections.ObservableList;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import java.time.Duration;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class OverviewActivityTest extends ActivityTest {

  @Override
  protected void createTestData(EntityManager em) {
    new OverviewDSTest().createTestData(em);

    Context context = new Context("context");
    em.persist(context);
    em.persist(new Task("task1").setContext(context).setEstimatedTime(Duration.ofMinutes(42)));
    em.persist(new Task("task2").setContext(context).setEstimatedTime(Duration.ofMinutes(12)));
  }

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return OverviewActivity.class;
  }

  @Test
  public void testContextFilling() throws Exception {
    OverviewContextualController contextController = activityController.getControllerInstance(OverviewContextualController.class);
    FXPlatform.invokeLater(() -> contextController.context.getSelectionModel().select("context"));
    activityController.waitForTasks();
    FXPlatform.waitForFX();

    ObservableList<Task> items = contextController.contextTasks.getItems();
    assertEquals(2, items.size());
    assertEquals("task2", items.get(0).getName());
    assertEquals("task1", items.get(1).getName());
  }

  @Test
  public void testScheduledFilling() throws Exception {
    OverviewScheduledController scheduledController = activityController.getControllerInstance(OverviewScheduledController.class);
    ObservableList<Task> items = scheduledController.scheduledTasks.getItems();
    assertEquals(2, items.size());
    assertEquals(OverviewDSTest.SCHEDULED_TODAY, items.get(0).getName());
    assertEquals(OverviewDSTest.SCHEDULED_TODAY_NOON, items.get(1).getName());

    items = scheduledController.proposedTasks.getItems();
    assertEquals(2, items.size());
    assertEquals(OverviewDSTest.PROPOSED_THIS_WEEK, items.get(0).getName());
    assertEquals(OverviewDSTest.PROPOSED_THIS_WEEK_DAY, items.get(1).getName());
  }

  @Test
  public void testThoughtCreation() throws Exception {
    OverviewAddThoughtController addThoughtController = activityController.getControllerInstance(OverviewAddThoughtController.class);
    assertTrue(addThoughtController.save.isDisabled());

    FXPlatform.invokeLater(() -> {
      addThoughtController.name.setText("testThought");
      addThoughtController.description.setText("One arrogant bastard please!");
      addThoughtController.onSave();
    });
    activityController.waitForTasks();
    FXPlatform.waitForFX();

    assertEquals("", addThoughtController.name.getText());
    assertEquals("", addThoughtController.description.getText());

    List<Thought> thoughts = PersistentWork.from(Thought.class);
    assertEquals(1, thoughts.size());
    assertEquals("testThought", thoughts.get(0).getName());
    assertThat(thoughts.get(0).getDescription(), Matchers.containsString("bastard"));


    FXPlatform.invokeLater(() -> addThoughtController.name.setText("test"));
    FXPlatform.waitForFX();
    assertFalse(addThoughtController.save.isDisabled());

    FXPlatform.invokeLater(() -> addThoughtController.name.setText("testThought"));
    FXPlatform.waitForFX();
    assertTrue(addThoughtController.save.isDisabled());
  }
}