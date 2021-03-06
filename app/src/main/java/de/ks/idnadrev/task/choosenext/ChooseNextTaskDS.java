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
package de.ks.idnadrev.task.choosenext;

import de.ks.datasource.ListDataSource;
import de.ks.idnadrev.entity.Task;

import java.util.List;
import java.util.function.Consumer;

public class ChooseNextTaskDS implements ListDataSource<Task> {
  @Override
  public List<Task> loadModel(Consumer<List<Task>> furtherProcessing) {
    return null;
  }

  @Override
  public void saveModel(List<Task> model, Consumer<List<Task>> beforeSaving) {

  }
}
