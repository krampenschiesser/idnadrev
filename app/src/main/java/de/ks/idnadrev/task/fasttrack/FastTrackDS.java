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

import de.ks.flatjsondb.PersistentWork;
import de.ks.idnadrev.entity.Task;
import de.ks.standbein.datasource.NewInstanceDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.function.Consumer;

public class FastTrackDS extends NewInstanceDataSource<Task> {
  private static final Logger log = LoggerFactory.getLogger(FastTrackDS.class);

  @Inject
  PersistentWork persistentWork;

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
    persistentWork.run(em -> {
      String name = model.getName();
      Task task = name == null ? null : persistentWork.forName(Task.class, name);
      if (task != null) {
        log.debug("Found existing task {}", task);
        beforeSaving.accept(task);
      } else {
        beforeSaving.accept(model);
        if (model.getName() != null && model.getName().length() > 0) {
          em.persist(model);
          log.debug("Creating new task {}", model);
        }
      }
    });
  }
}
