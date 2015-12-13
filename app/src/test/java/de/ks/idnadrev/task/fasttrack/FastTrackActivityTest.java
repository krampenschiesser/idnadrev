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

package de.ks.idnadrev.task.fasttrack;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class FastTrackActivityTest extends ActivityTest {
  private static final Logger log = LoggerFactory.getLogger(FastTrackActivityTest.class);

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return FastTrackActivity.class;
  }

  @Override
  protected void createTestData(EntityManager em) {
    Task existing = new Task("existing");
    existing.setDescription("desc");
    existing.setCreationTime(LocalDateTime.now().minusDays(1));
    existing.setFinishTime(LocalDateTime.now().minusDays(1).plusHours(2));
    em.persist(existing);

    WorkUnit unit = new WorkUnit(existing);
    unit.setStart(existing.getCreationTime().plusMinutes(15));
    unit.setEnd(existing.getFinishTime().minusMinutes(15));
    em.persist(unit);
  }

  @Test
  public void testExisting() throws Exception {
    FastTrack fastTrack = activityController.getControllerInstance(FastTrack.class);
    assertNotNull(fastTrack);
    assertNotNull(fastTrack.nameController);

    FXPlatform.invokeLater(() -> fastTrack.nameController.getInput().setText("Existing"));
    Thread.sleep(LastTextChange.WAIT_TIME);
    activityController.waitForTasks();
    FXPlatform.waitForFX();

    AsciiDocEditor description = fastTrack.description;
    assertEquals("desc", description.getText());
    FXPlatform.invokeLater(() -> description.setText(description.getText() + "\nnext"));
    FXPlatform.invokeLater(() -> fastTrack.finishTask());
    activityController.waitForDataSource();

    PersistentWork.wrap(() -> {
      List<Task> tasks = PersistentWork.from(Task.class);
      assertEquals(1, tasks.size());
      Task task = tasks.get(0);

      assertEquals("desc\nnext", task.getDescription());
      assertEquals(2, task.getWorkUnits().size());

      assertThat(task.getFinishTime(), Matchers.greaterThan(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0))));
    });
  }

  @Test
  public void testNew() throws Exception {
    FastTrack fastTrack = activityController.getControllerInstance(FastTrack.class);
    assertNotNull(fastTrack);
    assertNotNull(fastTrack.nameController);

    FXPlatform.invokeLater(() -> fastTrack.nameController.getInput().setText("bla"));
    Thread.sleep(LastTextChange.WAIT_TIME);
    activityController.waitForTasks();
    FXPlatform.waitForFX();

    AsciiDocEditor description = fastTrack.description;
    FXPlatform.invokeLater(() -> description.setText("blubber"));
    FXPlatform.invokeLater(() -> fastTrack.finishTask());
    activityController.waitForDataSource();
    PersistentWork.wrap(() -> {
      Task task = PersistentWork.forName(Task.class, "bla");
      assertNotNull(task);
      assertEquals("blubber", task.getDescription());
      assertEquals(1, task.getWorkUnits().size());
      WorkUnit workUnit = task.getWorkUnits().iterator().next();

      long creationTime = task.getCreationTime().withNano(0).toEpochSecond(ZoneOffset.UTC);
      long workUnitStart = workUnit.getStart().withNano(0).toEpochSecond(ZoneOffset.UTC);
      assertThat(workUnitStart - creationTime, Matchers.lessThan(2L));

      long finishTime = task.getFinishTime().withNano(0).toEpochSecond(ZoneOffset.UTC);
      long workUnitEnd = workUnit.getEnd().withNano(0).toEpochSecond(ZoneOffset.UTC);
      assertThat(workUnitEnd - finishTime, Matchers.lessThan(2L));
    });
  }

  @Test
  public void testExistingFirstThenNew() throws Exception {
    FastTrack fastTrack = activityController.getControllerInstance(FastTrack.class);
    assertNotNull(fastTrack);
    assertNotNull(fastTrack.nameController);

    FXPlatform.invokeLater(() -> fastTrack.nameController.getInput().setText("Existing"));
    Thread.sleep(LastTextChange.WAIT_TIME);
    activityController.waitForTasks();

    //now set to new task
    log.info("Now setting to new task");
    FXPlatform.invokeLater(() -> fastTrack.nameController.getInput().setText("bla"));
    Thread.sleep(LastTextChange.WAIT_TIME * 2);
    activityController.waitForTasks();
    FXPlatform.waitForFX();

    AsciiDocEditor description = fastTrack.description;
    assertEquals("", description.getText());
    assertEquals("bla", fastTrack.nameController.getInput().getText());

    FXPlatform.invokeLater(() -> fastTrack.finishTask());
    activityController.waitForDataSource();

    PersistentWork.wrap(() -> {
      List<Task> tasks = PersistentWork.from(Task.class);
      assertEquals(2, tasks.size());
      tasks.forEach(task -> {
        assertEquals(1, task.getWorkUnits().size());
      });
    });
  }
}