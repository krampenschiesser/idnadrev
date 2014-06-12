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

import de.ks.activity.ActivityController;
import de.ks.datasource.NewInstanceDataSource;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.Thought;
import de.ks.idnadrev.entity.WorkType;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.entity.NamedPersistentObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Consumer;

public class ThoughtToTaskDS extends NewInstanceDataSource<Task> {
  private static final Logger log = LoggerFactory.getLogger(ThoughtToTaskDS.class);

  private Thought fromThought;
  @Inject
  ActivityController controller;

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
    PersistentWork.run((em) -> {
      MainTaskInfo mainTaskInfo = controller.getControllerInstance(MainTaskInfo.class);

      String contextName = mainTaskInfo.contextController.getInput().textProperty().getValueSafe().trim();
      setToOne(model, Context.class, contextName, model::setContext);

      String workType = mainTaskInfo.workTypeController.getInput().textProperty().getValueSafe().trim();
      setToOne(model, WorkType.class, workType, model::setWorkType);

      model.setEstimatedTime(mainTaskInfo.getEstimatedDuration());
      em.persist(model);
    });
  }

  private <T extends NamedPersistentObject<T>> void setToOne(Task model, Class<T> clazz, String contextName, Consumer<T> consumer) {
    if (!contextName.isEmpty()) {
      Optional<T> first = PersistentWork.forNameLike(clazz, contextName).stream().findFirst();
      if (first.isPresent()) {
        consumer.accept(first.get());
      }
    }
  }

  @Override
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint instanceof Thought) {
      this.fromThought = (Thought) dataSourceHint;
    }
  }
}
