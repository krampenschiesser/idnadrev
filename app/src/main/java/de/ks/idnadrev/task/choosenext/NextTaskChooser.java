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

import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.TaskState;
import de.ks.option.Options;
import de.ks.persistence.PersistentWork;
import de.ks.reflection.PropertyPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Chooses the next best task according to the following rules:
 *
 * . extimated time is less than given parameter
 * . context matches
 * . task is not delegated
 * . task is not masked as later
 * . task is not finshed
 *
 * The resulting tasks are then sorted by comparison.
 * The task with the highest priority is returned.
 * The priority is calculated as follows:
 *
 * . each day will add a priority of 10 ( so one week ago, prio=10)
 * . if the task is marked as asap a prio of 100 will be added
 * . the prio for estimated time vs. the max time is calculated with the following formula:
 * +++
 * $$prio = ((maxTime - estimatedTime) * 10) / (maxTime)$$
 * +++
 * it is only calculated when the estimated time is larger than the NextTaskChooserOptions.timeThreshold
 *
 * The 3 calulated priorities (age,asap,estimatedTime) are then multiplied by the factors given in NextTaskChooserOptions
 *
 * If the setting NextTaskChooserOptions.completlyRandom() is set, everything is bypassed.
 */
public class NextTaskChooser {
  private static final Logger log = LoggerFactory.getLogger(NextTaskChooser.class);
  static final String KEY_ESTIMATED_DURATION = PropertyPath.property(Task.class, t -> t.getEstimatedTime());
  static final String KEY_STATE = PropertyPath.property(Task.class, t -> t.getState());
  static final String KEY_CONTEXT = PropertyPath.property(Task.class, t -> t.getContext());
  static final String KEY_CONTEXT_NAME = PropertyPath.property(Context.class, t -> t.getName());
  static final String KEY_FINISHTIME = PropertyPath.property(Task.class, t -> t.getFinishTime());

  protected NextTaskChooserOptions options;

  public NextTaskChooser() {
    options = Options.get(NextTaskChooserOptions.class);
  }

  public List<Task> getTasksSorted(int minutes, String selectedContext) {
    List<Task> allPossibleTasks = getAllPossibleTasks(minutes, selectedContext);
    if (allPossibleTasks.isEmpty()) {
      log.info("No tasks to choose from.");
      return Collections.emptyList();
    } else {
      List<Task> tasks = sortTasksByPrio(minutes, allPossibleTasks);
      return tasks;
    }
  }

  private String tasks2NameString(List<Task> tasks) {
    return tasks.stream().map(t -> t.getName()).reduce("", (o1, o2) -> o1 + ", " + o2);
  }

  protected List<Task> sortTasksByPrio(int maxTime, List<Task> allPossibleTasks) {
    log.debug("Got {} tasks to choose from: {}", allPossibleTasks.size(), tasks2NameString(allPossibleTasks));
    allPossibleTasks = new ArrayList<>(allPossibleTasks);

    Collections.sort(allPossibleTasks, Comparator.comparing(task -> {
      long ageInDays = Duration.between(task.getCreationTime(), LocalDateTime.now()).toDays();
      long agePrio = ageInDays * 10;
      long asapPrio = task.getState() == TaskState.ASAP ? 100 : 0;

      long estimatedMinutes = task.getEstimatedTime().toMinutes();

      long matchingTimePrio;
      if (estimatedMinutes > options.getTimeThreshold()) {
        matchingTimePrio = (long) ((maxTime - estimatedMinutes) * 10D / maxTime);
      } else {
        matchingTimePrio = 0;
      }


      agePrio *= options.getAgeFactor();
      asapPrio *= options.getAsapFactor();
      matchingTimePrio *= options.getTimeFactor();
      long totalPrio = agePrio + asapPrio + matchingTimePrio;
      log.debug("Task {} has prio {}", task.getName(), totalPrio);
      return totalPrio;
    }));
    Collections.reverse(allPossibleTasks);
    log.debug("Sorted tasks to the following order: {}", tasks2NameString(allPossibleTasks));
    return allPossibleTasks;
  }

  protected List<Task> getAllPossibleTasks(int minutes, String selectedContext) {
    List<Task> retval = PersistentWork.wrap(() -> {
      List<Task> tasks = PersistentWork.from(Task.class, (root, query, builder) -> {
        Join<Task, Context> join = root.join(KEY_CONTEXT);
        join.on(builder.equal(join.get(KEY_CONTEXT_NAME), selectedContext));


        Path<Object> state = root.get(KEY_STATE);
        Predicate notLater = builder.notEqual(state, TaskState.LATER);
        Predicate notDelegated = builder.notEqual(state, TaskState.DELEGATED);
        Predicate notFinished = builder.isNull(root.get(KEY_FINISHTIME));

        query.where(notFinished, notLater, notDelegated);
      }, null);

      return tasks.stream()//super ugly, need to evict to save heap
              .sorted(Comparator.comparing(c -> c.getEstimatedTime().toMinutes() - c.getSpentMinutes()))//
              .filter(t -> {
                long timeRemaining = t.getEstimatedTime().toMinutes() - t.getSpentMinutes();
                log.info("Remaining time: {}", timeRemaining);
                return timeRemaining < minutes && timeRemaining > 2;
              })//
              .collect(Collectors.toList());
    });
    return retval;
  }
}

