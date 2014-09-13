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

package de.ks.idnadrev.task.create;

import de.ks.activity.ActivityController;
import de.ks.datasource.NewInstanceDataSource;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.Thought;
import de.ks.persistence.PersistentWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.function.Consumer;

public class CreateTaskDS extends NewInstanceDataSource<Task> {
  private static final Logger log = LoggerFactory.getLogger(CreateTaskDS.class);

  Thought fromThought;
  Task fromTask;

  @Inject
  ActivityController controller;

  public CreateTaskDS() {
    super(Task.class);
  }

  @Override
  public Task loadModel(Consumer<Task> furtherProcessing) {
    return PersistentWork.wrap(() -> {
      if (fromTask != null) {
        Task task = PersistentWork.reload(fromTask);
        furtherProcessing.accept(task);
        return task;
      } else {
        Task task = super.loadModel(furtherProcessing);
        if (fromThought != null) {
          Thought reloaded = PersistentWork.reload(fromThought);
          task.setName(reloaded.getName());
          task.setDescription(reloaded.getDescription());
          task.getFiles().addAll(reloaded.getFiles());
        }
        task.setProject(false);
        furtherProcessing.accept(task);
        return task;
      }
    });
  }

  @Override
  public void saveModel(Task model, Consumer<Task> beforeSaving) {
    PersistentWork.run((em) -> {
      Task task = PersistentWork.reload(model);
      task.setFinished(false);
      beforeSaving.accept(task);
      em.persist(task);
      if (fromThought != null && fromThought.getId() > 0) {
        em.remove(PersistentWork.reload(fromThought));
      }
    });
    resetFrom();
  }

  @Override
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint instanceof Thought) {
      this.fromThought = (Thought) dataSourceHint;
    }
    if (dataSourceHint instanceof Task) {
      this.fromTask = (Task) dataSourceHint;
    }
  }

  public void resetFrom() {
    fromTask = null;
    fromThought = null;
  }
}
