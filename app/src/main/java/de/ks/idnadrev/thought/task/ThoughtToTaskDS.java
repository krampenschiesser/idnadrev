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

package de.ks.idnadrev.thought.task;

import de.ks.datasource.NewInstanceDataSource;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.Thought;

public class ThoughtToTaskDS extends NewInstanceDataSource<Task> {
  private Thought fromThought;

  public ThoughtToTaskDS() {
    super(Task.class);
  }

  @Override
  public Task loadModel() {
    Task task = super.loadModel();
    if (fromThought != null) {
      task.setName(fromThought.getName());
      task.setDescription(fromThought.getDescription());
    }
    return task;
  }

  @Override
  public void saveModel(Task model) {

  }

  @Override
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint instanceof Thought) {
      this.fromThought = (Thought) dataSourceHint;
    }
  }
}
