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
import de.ks.idnadrev.entity.WorkUnit;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.QueryConsumer;
import de.ks.persistence.entity.NamedPersistentObject;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ViewTasksDS implements ListDataSource<Task> {
  private Task taskToSelect;
  private QueryConsumer<Task, Task> filter;

  @Inject
  ActivityInitialization initialization;

  @Override
  public List<Task> loadModel(Consumer<List<Task>> furtherProcessing) {
    TaskFilterView taskFilterView = initialization.getControllerInstance(TaskFilterView.class);
    taskFilterView.applyFilterOnDS(this);

    return PersistentWork.wrap(() -> {
      List<Task> from = PersistentWork.read((em) -> {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Task> query = builder.createQuery(Task.class);

        Root<Task> root = query.from(Task.class);
        query.select(root);

        if (filter != null) {
          filter.accept(root, query, builder);
        }

        List<Task> resultList = em.createQuery(query).getResultList();
        Task parentTask = PersistentWork.reload(taskFilterView.getParentTask());
        if (parentTask != null) {
          resultList = resultList.stream().filter(t -> t.hasParent(parentTask)).collect(Collectors.toList());
          if (!resultList.contains(parentTask)) {
            resultList.add(parentTask);
          }
        }
        resultList.forEach(this::loadChildren);
        return resultList;
      });
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
    task.getTags().forEach(NamedPersistentObject::getName);
    task.getWorkUnits().forEach(WorkUnit::getDuration);
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

  public void setFilter(QueryConsumer<Task, Task> filter) {
    this.filter = filter;
  }
}
