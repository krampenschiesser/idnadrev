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
package de.ks.idnadrev.thought.view;

import de.ks.idnadrev.adoc.AdocFile;
import de.ks.idnadrev.index.Index;
import de.ks.idnadrev.index.StandardQueries;
import de.ks.idnadrev.task.Task;
import de.ks.idnadrev.task.TaskState;
import de.ks.standbein.datasource.ListDataSource;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class ViewThoughtsDs implements ListDataSource<Task> {
  @Inject
  Index index;

  @Override
  public List<Task> loadModel(Consumer<List<Task>> furtherProcessing) {
    ArrayList<Task> tasks = new ArrayList<>(index.query(Task.class, StandardQueries.stateQuery(), s -> s == TaskState.UNPROCESSED));
    Collections.sort(tasks, Comparator.comparing(AdocFile::getTitle));
    return tasks;
  }

  @Override
  public void saveModel(List<Task> model, Consumer<List<Task>> beforeSaving) {

  }
}
