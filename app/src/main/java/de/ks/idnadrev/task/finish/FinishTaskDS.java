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
package de.ks.idnadrev.task.finish;

import de.ks.datasource.DataSource;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.WorkUnit;
import de.ks.persistence.PersistentWork;

import java.util.SortedSet;
import java.util.function.Consumer;

public class FinishTaskDS implements DataSource<Task> {
  private Task task;

  @Override
  public Task loadModel(Consumer<Task> furtherProcessing) {
    return PersistentWork.wrap(() -> {
      Task reload = PersistentWork.reload(task, t -> t.getWorkUnits().forEach(u -> u.getStart()));

      SortedSet<WorkUnit> workUnits = reload.getWorkUnits();
      if (!workUnits.isEmpty()) {
        workUnits.last().stop();
      }
      reload.setFinished(true);
      furtherProcessing.accept(reload);
      return reload;
    });
  }

  @Override
  public void saveModel(Task model, Consumer<Task> beforeSaving) {
    PersistentWork.wrap(() -> {
      Task reload = PersistentWork.reload(model);
      beforeSaving.accept(reload);
    });
  }

  @Override
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint instanceof Task) {
      this.task = (Task) dataSourceHint;
    }
  }
}
