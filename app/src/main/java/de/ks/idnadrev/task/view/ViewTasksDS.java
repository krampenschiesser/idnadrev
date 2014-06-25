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
package de.ks.idnadrev.task.view;

import de.ks.datasource.ListDataSource;
import de.ks.idnadrev.entity.Task;
import de.ks.persistence.PersistentWork;
import de.ks.reflection.PropertyPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ViewTasksDS implements ListDataSource<Task> {
  private static final Logger log = LoggerFactory.getLogger(ViewTasksDS.class);
  private Task taskToSelect;

  @Override
  public List<Task> loadModel() {
    return PersistentWork.from(Task.class, (root, query, builder) -> {
      String finishTime = PropertyPath.property(Task.class, (t) -> t.getFinishTime());
      query.where(root.get(finishTime).isNull());
    }, this::loadChildren);
  }

  protected void loadChildren(Task task) {
    task.getName();
    if (task.getParent() != null) {
      task.getParent().getName();
    }
    if (task.getContext() != null) {
      task.getContext().getName();
    }
    task.getChildren().forEach(this::loadChildren);
    task.getTags().forEach(tag -> tag.getName());
    task.getWorkUnits().forEach(workUnit -> workUnit.getDuration());
  }

  @Override
  public void saveModel(List<Task> model) {
    //noop
  }

  @Override
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint instanceof Task) {
      this.taskToSelect = (Task) dataSourceHint;
    }
  }

  public Task getTaskToSelect() {
    return taskToSelect;
  }
}
