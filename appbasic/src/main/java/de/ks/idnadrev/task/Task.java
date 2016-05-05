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
package de.ks.idnadrev.task;

import de.ks.idnadrev.adoc.AdocFile;
import de.ks.idnadrev.adoc.Header;
import de.ks.idnadrev.repository.Repository;
import de.ks.idnadrev.task.cron.CronTab;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

public class Task extends AdocFile {
  protected String context;
  protected TaskState state = TaskState.NONE;
  protected Duration estimatedTime;
  protected CronTab cronTab;
  protected LocalDateTime finishTime;
  protected String delegation;

  public Task(Path path, Repository repository, Header header) {
    super(path, repository, header);
  }

  @Override
  public Task setFileName(String fileName) {
    super.setFileName(fileName);
    return this;
  }

  public boolean isFinished() {
    return finishTime != null;
  }

  public boolean isProject() {
    return estimatedTime == null;
  }

  public boolean isDelegated() {
    return delegation != null;
  }

  public boolean isThought() {
    return state == TaskState.UNPROCESSED;
  }

  public CronTab getCronTab() {
    return cronTab;
  }

  public String getContext() {
    return getHeader().getHeaderElement("context");
  }

  public Task setContext(String context) {
    getHeader().setHeaderElement("context", context);
    return this;
  }

  public Duration getEstimatedTime() {
    return estimatedTime;
  }

  public String getDelegation() {
    return delegation;
  }

  public LocalDateTime getFinishTime() {
    return finishTime;
  }

  public TaskState getState() {
    String state = header.getHeaderElement("state");
    if (state == null) {
      return TaskState.NONE;
    } else {
      return TaskState.valueOf(state);
    }
  }

  public Task setState(TaskState state) {
    header.setHeaderElement("state", state.name());
    return this;
  }
}
