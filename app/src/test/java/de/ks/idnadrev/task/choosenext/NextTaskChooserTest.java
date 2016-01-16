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

import de.ks.flatadocdb.session.Session;
import de.ks.flatjsondb.PersistentWork;
import de.ks.idnadrev.IdnadrevIntegrationTestModule;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.TaskState;
import de.ks.idnadrev.entity.WorkUnit;
import de.ks.standbein.LoggingGuiceTestSupport;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NextTaskChooserTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IdnadrevIntegrationTestModule()).launchServices();
  @Inject
  NextTaskChooser chooser;
  @Inject
  PersistentWork persistentWork;

  @Before
  public void setUp() throws Exception {
    persistentWork.run(em -> {
      createTestData(em);
    });
  }

  static void createTestData(Session em) {
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

    Task taskWithWorkUnit = new Task("workTask9MinRemaining").setEstimatedTime(Duration.ofMinutes(20)).setContext(workContext);
    WorkUnit workUnit = taskWithWorkUnit.start();
    LocalDateTime start = LocalDateTime.now().minusDays(1);
    workUnit.setStart(start);
    workUnit.setEnd(start.plusMinutes(11));
    taskWithWorkUnit.getWorkUnits().add(workUnit);
    em.persist(taskWithWorkUnit);

    Task taskOverTime = new Task("taskOverTime").setEstimatedTime(Duration.ofMinutes(20)).setContext(workContext);
    workUnit = taskOverTime.start();
    workUnit.setStart(start.plusHours(1));
    workUnit.setEnd(start.plusHours(1).plusMinutes(60));
    taskOverTime.getWorkUnits().add(workUnit);
    em.persist(taskOverTime);

    em.persist(new Task("otherTask").setContext(other));
    em.persist(new Task("taskWithoutContext").setEstimatedTime(Duration.ofMinutes(8)));
  }

  @Test
  public void testAllPossibleTasks() throws Exception {
    List<Task> allPossibleTasks = chooser.getAllPossibleTasks(10, "work");
    assertEquals(3, allPossibleTasks.size());
    assertEquals("workTask5Min", allPossibleTasks.get(0).getName());
    assertEquals("workTask8Min", allPossibleTasks.get(1).getName());
    assertEquals("workTask9MinRemaining", allPossibleTasks.get(2).getName());
  }

  @Test
  public void testTasksWithoutContext() throws Exception {
    List<Task> allPossibleTasks = chooser.getAllPossibleTasks(10, null);
    assertEquals(1, allPossibleTasks.size());
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