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

package de.ks.idnadrev.task.choosenext;

import de.ks.LauncherRunner;
import de.ks.idnadrev.entity.Cleanup;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.TaskState;
import de.ks.persistence.PersistentWork;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class NextTaskChooserTest {
  @Inject
  NextTaskChooser chooser;
  @Inject
  Cleanup cleanup;

  @Before
  public void setUp() throws Exception {
    cleanup.cleanup();
    PersistentWork.run(em -> {
      createTestData(em);
    });
  }

  static void createTestData(EntityManager em) {
    Context workContext = new Context("work");
    Context other = new Context("other");
    em.persist(workContext);
    em.persist(other);

    em.persist(new Task("workTaskNoTime").setContext(workContext));
    em.persist(new Task("workTask5MinFinished").setFinished(true).setEstimatedTime(Duration.ofMinutes(5)).setContext(workContext));
    em.persist(new Task("workTask5MinLater").setState(TaskState.LATER).setEstimatedTime(Duration.ofMinutes(5)).setContext(workContext));
    em.persist(new Task("workTask5MinDelegated").setState(TaskState.DELEGATED).setEstimatedTime(Duration.ofMinutes(5)).setContext(workContext));
    em.persist(new Task("workTask2Min").setEstimatedTime(Duration.ofMinutes(2)).setContext(workContext));

    em.persist(new Task("workTask5Min").setEstimatedTime(Duration.ofMinutes(5)).setContext(workContext));
    em.persist(new Task("workTask8Min").setEstimatedTime(Duration.ofMinutes(8)).setContext(workContext));
    em.persist(new Task("workTask15Min").setEstimatedTime(Duration.ofMinutes(15)).setContext(workContext));

    em.persist(new Task("otherTask").setContext(other));
  }

  @Test
  public void testAllPossibleTasks() throws Exception {
    List<Task> allPossibleTasks = chooser.getAllPossibleTasks(10, "work");
    assertEquals(2, allPossibleTasks.size());
    assertEquals("workTask5Min", allPossibleTasks.get(0).getName());
    assertEquals("workTask8Min", allPossibleTasks.get(1).getName());
  }

  @Test
  public void testWeightingAsapVsOld() throws Exception {
    Task veryOld = new Task("veryOld");
    veryOld.setCreationTime(LocalDateTime.now().minusDays(7));

    Task asap = new Task("asap");
    asap.setState(TaskState.ASAP);

    List<Task> sorted = chooser.sortTasksByPrio(10, Arrays.asList(veryOld, asap));
    assertEquals(asap, sorted.get(0));

    veryOld.setCreationTime(LocalDateTime.now().minusDays(11));

    sorted = chooser.sortTasksByPrio(10, Arrays.asList(veryOld, asap));
    assertEquals(veryOld, sorted.get(0));
  }

  @Test
  public void testWeighingTimeVsAge() throws Exception {
    Task veryOld = new Task("veryOld");
    veryOld.setCreationTime(LocalDateTime.now().minusDays(7));

    Task timed = new Task("timed");
    timed.setEstimatedTime(Duration.ofMinutes(3));

    List<Task> tasks = Arrays.asList(veryOld, timed);

    List<Task> sorted = chooser.sortTasksByPrio(20, tasks);
    assertEquals(timed, sorted.get(0));

    timed.setEstimatedTime(Duration.ofMinutes(2));//threshold
    sorted = chooser.sortTasksByPrio(20, tasks);
    assertEquals(veryOld, sorted.get(0));

    timed.setEstimatedTime(Duration.ofMinutes(10));
    sorted = chooser.sortTasksByPrio(20, tasks);
    assertEquals(veryOld, sorted.get(0));
  }
}