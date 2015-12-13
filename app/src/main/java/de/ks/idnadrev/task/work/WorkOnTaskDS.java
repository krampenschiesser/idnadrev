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

import de.ks.flatjsondb.PersistentWork;
import de.ks.idnadrev.entity.Task;
import de.ks.standbein.datasource.DataSource;

import javax.inject.Inject;
import java.util.function.Consumer;

public class WorkOnTaskDS implements DataSource<Task> {
  private Task task;

  @Inject
  PersistentWork persistentWork;

  @Override
  public Task loadModel(Consumer<Task> furtherProcessing) {
    persistentWork.run(session -> {
      Task reloaded = persistentWork.reload(task);
      furtherProcessing.accept(reloaded);
      task = reloaded;
    });
    return task;
  }

  @Override
  public void saveModel(Task model, Consumer<Task> beforeSaving) {
    persistentWork.run(session -> {
      Task reloaded = persistentWork.reload(model);
      beforeSaving.accept(reloaded);
//      reloaded.getWorkUnits().last().stop();
    });
  }

  @Override
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint instanceof Task) {
      task = ((Task) dataSourceHint);

    }
  }
}
