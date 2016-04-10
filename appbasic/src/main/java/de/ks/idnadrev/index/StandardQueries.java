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
package de.ks.idnadrev.index;

import de.ks.idnadrev.adoc.AdocFile;
import de.ks.idnadrev.task.Task;
import de.ks.idnadrev.task.TaskState;
import de.ks.idnadrev.task.cron.CronTab;

import java.util.Set;

public class StandardQueries {
  public static Query<Task, Boolean> finishedQuery() {
    return Query.of(Task.class, Task::isFinished);
  }

  public static Query<Task, CronTab> crontabQuery() {
    return Query.of(Task.class, Task::getCronTab);
  }

  public static Query<Task, TaskState> stateQuery() {
    return Query.of(Task.class, Task::getState);
  }

  public static Query<Task, String> contextQuery() {
    return Query.of(Task.class, Task::getContext);
  }

  public static Query<AdocFile, Set<String>> byTagsQuery() {
    return Query.of(AdocFile.class, f -> f.getHeader().getTags());
  }

  public static Query<AdocFile, String> titleQuery() {
    return Query.of(AdocFile.class, f -> f.getHeader().getTitle());
  }
}
