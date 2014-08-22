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

import de.ks.datasource.NewInstanceDataSource;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.WorkUnit;
import de.ks.persistence.PersistentWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.function.Consumer;

public class FastTrackDS extends NewInstanceDataSource<Task> {
  private static final Logger log = LoggerFactory.getLogger(FastTrackDS.class);

  public FastTrackDS() {
    super(Task.class);
  }

  @Override
  public Task loadModel(Consumer<Task> furtherProcessing) {
    Task task = super.loadModel(furtherProcessing);
    furtherProcessing.accept(task);
    return task;
  }

  @Override
  public void saveModel(Task model, Consumer<Task> beforeSaving) {
    PersistentWork.run(em -> {
      String name = model.getName();
      Task task = name == null ? null : PersistentWork.forName(Task.class, name);
      if (task != null) {
        log.debug("Found existing task {}", task);
        beforeSaving.accept(task);
      } else {
        beforeSaving.accept(model);
        log.debug("Creating new task {}", model);
        em.persist(model);
        task = model;
      }
      WorkUnit workUnit = new WorkUnit(task);
      workUnit.setStart(model.getCreationTime());
      LocalDateTime now = LocalDateTime.now();
      workUnit.setEnd(now);
      task.setFinishTime(now);
      em.persist(workUnit);
    });
  }
}
