/*
 * Copyright [2016] [Christian Loehnert]
 *
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

import de.ks.idnadrev.adoc.AdocFile;
import de.ks.idnadrev.index.Index;
import de.ks.idnadrev.index.MultiQueyBuilder;
import de.ks.idnadrev.index.StandardQueries;
import de.ks.idnadrev.repository.Repository;
import de.ks.idnadrev.task.Task;
import de.ks.idnadrev.task.TaskState;
import de.ks.standbein.datasource.ListDataSource;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ViewTasksDs implements ListDataSource<Task> {
  @Inject
  Index index;
  private volatile TaskDsFilter filter;

  @Override
  public List<Task> loadModel(Consumer<List<Task>> furtherProcessing) {
    MultiQueyBuilder<Task> query = index.multiQuery(Task.class);
    query.query(StandardQueries.finishedQuery(), s -> !s);
    query.query(StandardQueries.stateQuery(), s -> s != TaskState.UNPROCESSED);
    Set<Task> found = query.find();
    if (filter != null) {
      found = found.parallelStream().filter(filter).collect(Collectors.toSet());
    }
    List<Task> tasks = new ArrayList<>(found);
    Collections.sort(tasks, Comparator.comparing(AdocFile::getTitle));
    return tasks;
  }

  @Override
  public void saveModel(List<Task> model, Consumer<List<Task>> beforeSaving) {

  }

  @Override
  @SuppressWarnings("unchecked")
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint instanceof TaskDsFilter) {
      filter = (TaskDsFilter) dataSourceHint;
    }
  }

  public static class TaskDsFilter implements Predicate<Task> {
    final ArrayList<Predicate<Task>> filters = new ArrayList<>();

    public TaskDsFilter(String title, String context, String state, List<String> selectedTags, String repository, String wildCard) {
      Objects.requireNonNull(title);
      Objects.requireNonNull(context);
      Objects.requireNonNull(state);
      Objects.requireNonNull(repository);
      Objects.requireNonNull(wildCard);

      if (!title.isEmpty()) {
        filters.add(t -> t.getTitle().toLowerCase(Locale.ROOT).trim().contains(title));
      }
      if (!context.isEmpty() && !wildCard.equals(context)) {
        filters.add(t -> Optional.ofNullable(t.getContext()).map(s -> s.toLowerCase(Locale.ROOT).trim()).orElse("").equals(context));
      }
      if (!selectedTags.isEmpty()) {
        filters.add(t -> !Collections.disjoint(t.getHeader().getTags(), selectedTags));
      }
      if (!repository.isEmpty() && !wildCard.equals(repository)) {
        filters.add(t -> Optional.ofNullable(t.getRepository()).map(Repository::getName).map(s -> s.toLowerCase(Locale.ROOT).trim()).orElse("").equals(repository));
      }
      if (!state.isEmpty() && !wildCard.equals(state)) {
        filters.add(t -> Optional.ofNullable(t.getState()).map(s -> s.name().toLowerCase(Locale.ROOT).trim()).orElse("").equals(state));
      }
    }

    @Override
    public boolean test(Task task) {
      for (Predicate<Task> filter : filters) {
        if (!filter.test(task)) {
          return false;
        }
      }
      return true;
    }
  }
}
