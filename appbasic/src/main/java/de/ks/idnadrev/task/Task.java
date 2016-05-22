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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

public class Task extends AdocFile {
  private static final Logger log = LoggerFactory.getLogger(Task.class);
  public static final String ESTIMATEDTIME = "estimatedtime";
  public static final String DELEGATION = "delegation";
  public static final String STATE = "state";
  public static final String CONTEXT = "context";
  public static final String KSTYPE = "kstype";
  protected LocalDateTime finishTime;

  public Task(Path path, Repository repository, Header header) {
    super(path, repository, header.setHeaderElement(KSTYPE, "task"));
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
    return getEstimatedTime() == null;
  }

  public boolean isDelegated() {
    return getDelegation() != null;
  }

  public boolean isThought() {
    return getState() == TaskState.UNPROCESSED;
  }

  public CronTab getCronTab() {
    String cron = getHeader().getHeaderElement("cron");
    if (cron == null) {
      return null;
    } else {
      try {
        return new CronTab().parse(cron);
      } catch (IllegalArgumentException e) {
        log.error("Could not parse crontab {}", cron, e);
        return null;
      }
    }
  }

  public String getContext() {
    return getHeader().getHeaderElement(CONTEXT);
  }

  public Task setContext(String context) {
    getHeader().setHeaderElement(CONTEXT, context);
    return this;
  }

  public Task setEstimatedTimeInMinutes(@Nullable Integer minutes) {
    getHeader().setHeaderElement(ESTIMATEDTIME, minutes == null ? null : String.valueOf(minutes));
    return this;
  }

  public Duration getEstimatedTime() {
    String estimatedtime = getHeader().getHeaderElement(ESTIMATEDTIME);
    if (estimatedtime == null) {
      return null;
    } else {
      try {
        int i = Integer.parseInt(estimatedtime);
        return Duration.ofMinutes(i);
      } catch (NumberFormatException e) {
        log.error("Could not parse duration {}", estimatedtime, e);
        return null;
      }
    }
  }

  public int getEstimatedTimeInMinutes() {
    Duration estimatedTime = getEstimatedTime();
    if (estimatedTime == null) {
      return 0;
    } else {
      return (int) estimatedTime.toMinutes();
    }
  }

  public String getDelegation() {
    return getHeader().getHeaderElement(DELEGATION);
  }

  public LocalDateTime getFinishTime() {
    return finishTime;
  }

  public TaskState getState() {
    String state = header.getHeaderElement(STATE);
    if (state == null) {
      return TaskState.NONE;
    } else {
      return TaskState.valueOf(state);
    }
  }

  public Task setState(TaskState state) {
    header.setHeaderElement(STATE, state.name());
    return this;
  }
}
