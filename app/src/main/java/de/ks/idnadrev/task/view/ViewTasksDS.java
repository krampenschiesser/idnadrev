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

import de.ks.flatjsondb.PersistentWork;
import de.ks.idnadrev.entity.Task;
import de.ks.standbein.activity.initialization.ActivityInitialization;
import de.ks.standbein.datasource.ListDataSource;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ViewTasksDS implements ListDataSource<Task> {
  private Task taskToSelect;
//  private QueryConsumer<Task, Task> filter;

  @Inject
  ActivityInitialization initialization;
  @Inject
  PersistentWork persistentWork;

  @Override
  public List<Task> loadModel(Consumer<List<Task>> furtherProcessing) {

    TaskFilterView taskFilterView = initialization.getControllerInstance(TaskFilterView.class);
    String searchContent = taskFilterView.getSearchContent().trim().toLowerCase(Locale.ROOT);
    int maxResults = taskFilterView.getMaxResults();

    List<Task> result = persistentWork.read(session -> {
      List<Task> tasks = new ArrayList<Task>(persistentWork.multiQuery(Task.class, taskFilterView::applyFilterOnDS));
      tasks.forEach(this::loadChildren);
      furtherProcessing.accept(tasks);
      return tasks;
    });

    return result;
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

  // FIXME: 12/15/15
//  public void setFilter(QueryConsumer<Task, Task> filter) {
//    this.filter = filter;
//  }
}
