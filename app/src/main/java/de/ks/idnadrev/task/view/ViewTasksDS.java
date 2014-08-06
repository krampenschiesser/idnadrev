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

import de.ks.activity.initialization.ActivityInitialization;
import de.ks.datasource.ListDataSource;
import de.ks.idnadrev.entity.Task;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.QueryConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import java.util.List;
import java.util.function.Consumer;

public class ViewTasksDS implements ListDataSource<Task> {
  private static final Logger log = LoggerFactory.getLogger(ViewTasksDS.class);
  private Task taskToSelect;
  private QueryConsumer<Task> filter;

  @Override
  public List<Task> loadModel(Consumer<List<Task>> furtherProcessing) {
    TaskFilterView taskFilterView = CDI.current().select(ActivityInitialization.class).get().getControllerInstance(TaskFilterView.class);
    taskFilterView.applyFilterOnDS(this);

    return PersistentWork.wrap(() -> {
      List<Task> from = PersistentWork.from(Task.class, (root, query, builder) -> {
        if (filter != null) {
          filter.accept(root, query, builder);
        }
      }, this::loadChildren);

      furtherProcessing.accept(from);
      return from;
    });
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
  public void saveModel(List<Task> model, Consumer<List<Task>> beforeSaving) {
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

  public void setFilter(QueryConsumer<Task> filter) {
    this.filter = filter;
  }
}
