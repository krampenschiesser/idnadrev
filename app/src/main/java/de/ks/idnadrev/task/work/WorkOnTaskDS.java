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
package de.ks.idnadrev.task.work;

import de.ks.datasource.DataSource;
import de.ks.idnadrev.entity.Task;
import de.ks.persistence.PersistentWork;

public class WorkOnTaskDS implements DataSource<Task> {
  private Task task;

  @Override
  public Task loadModel() {
    return task;
  }

  @Override
  public void saveModel(Task model) {
    PersistentWork.run(em -> {
      Task task = model;
      task = PersistentWork.byId(Task.class, task.getId());
      task.getWorkUnits().last().stop();
    });
  }

  @Override
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint instanceof Task) {
      task = ((Task) dataSourceHint);

    }
  }
}
