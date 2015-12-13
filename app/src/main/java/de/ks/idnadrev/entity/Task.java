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

package de.ks.idnadrev.entity;

import de.ks.flatadocdb.annotation.*;
import de.ks.flatadocdb.entity.NamedEntity;
import de.ks.idnadrev.entity.information.Information;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
public class Task extends NamedEntity implements FileContainer<Task>, Tagged {
  private static final long serialVersionUID = 1L;
  private static final String TASK_TAG_JOINTABLE = "task_tag";

  protected String description;

  //  @ToMany
  protected SortedSet<WorkUnit> workUnits = new TreeSet<>();

  protected LocalDateTime finishTime;

  protected Duration estimatedTime;

  protected LocalDate dueDate;//must be done before this date (and time)
  protected LocalTime dueTime;//if null, only date relevant

  @Child
  protected Schedule schedule;

  @ToOne
  protected Task parent;

  @Children
  protected Set<Task> children = new HashSet<>();

  protected TaskState state = TaskState.NONE;
  protected String delegationReason;

  @ToOne
  protected Context context;

  protected final Effort physicalEffort = new Effort(Effort.EffortType.PHSYICAL);
  protected final Effort mentalEffort = new Effort(Effort.EffortType.MENTAL);
  protected final Effort funFactor = new Effort(Effort.EffortType.FUN);

  protected boolean project;

  protected Outcome outcome = new Outcome();

  protected Set<Tag> tags = new HashSet<>();

  @Children
  protected Set<FileReference> files = new HashSet<>();

  @ToMany
  protected Set<Information> informations = new HashSet<>();

  protected Task() {
    super(null);
  }

  public Task(String name) {
    super(name);
  }

  public Task(String name, String description) {
    this(name);
    this.description = description;
  }

  @Override
  public Set<FileReference> getFiles() {
    return files;
  }

  public String getDescription() {
    return description;
  }

  public String getShortDescription() {
    if ((description != null) && (description.length() > 50)) {
      return description.substring(0, 50);
    } else {
      return description;
    }
  }

  public Task setDescription(String description) {
    this.description = description;
    return this;
  }

  public long getSpentMinutes() {
    Duration duration = Duration.ZERO;
    for (WorkUnit workUnit : getWorkUnits()) {
      duration = duration.plus(workUnit.getDuration());
    }
    return duration.toMinutes();
  }

  public void start() {
    WorkUnit workUnit = new WorkUnit(this);
    getWorkUnits().add(workUnit);
  }

  public void stop() {
    WorkUnit last = getWorkUnits().last();
    if (last.getEnd() == null) {
      last.stop();
    }
  }

  public Task setFinished(boolean finished) {
    if (finished) {
      setState(TaskState.NONE);
      this.finishTime = LocalDateTime.now();
    } else {
      this.finishTime = null;
    }
    return this;
  }

  public void setFinishTime(LocalDateTime finishTime) {
    this.finishTime = finishTime;
  }

  public boolean isFinished() {
    return finishTime != null;
  }

  public LocalDateTime getFinishTime() {
    return finishTime;
  }

  public SortedSet<WorkUnit> getWorkUnits() {
    return workUnits;
  }

  public Task getParent() {
    return parent;
  }

  public boolean hasParent(Task possibleParent) {
    for (Task current = this; current.getParent() != null; current = current.getParent()) {
      if (current.getParent().getId() == possibleParent.getId()) {
        return true;
      }
    }
    return false;
  }

  public void setParent(Task parent) {
    if (this.parent != null) {
      this.parent.getChildren().remove(this);
    }
    this.parent = parent;
    if (parent != null) {
      parent.addChild(this);
    }
  }

  public TaskState getState() {
    return state;
  }

  public Task setState(TaskState state) {
    this.state = state;
    return this;
  }

  public String getDelegationReason() {
    return delegationReason;
  }

  public Task setDelegationReason(String delegationReason) {
    this.delegationReason = delegationReason;
    return this;
  }

  public Context getContext() {
    return context;
  }

  public Task setContext(Context context) {
    this.context = context;
    return this;
  }

  public Effort getPhysicalEffort() {
    return physicalEffort;
  }

  public Effort getMentalEffort() {
    return mentalEffort;
  }

  public Effort getFunFactor() {
    return funFactor;
  }

  public boolean isProject() {
    return project;
  }

  public Task setProject(boolean project) {
    this.project = project;
    return this;
  }

  public Set<Task> getChildren() {
    return children;
  }

  public Task addChild(Task child) {
    getChildren().add(child);
    if (!this.equals(child.getParent())) {
      child.setParent(this);
    }
    this.setProject(true);
    return this;
  }

  public Duration getEstimatedTime() {
    if (estimatedTime == null) {
      return Duration.ofMinutes(0);
    } else {
      return estimatedTime;
    }
  }

  public Duration getRemainingTime() {
    return getEstimatedTime().minus(Duration.ofMinutes(getSpentMinutes()));
  }

  public Task setEstimatedTime(Duration estimatedTime) {
    this.estimatedTime = estimatedTime;
    return this;
  }

  @Override
  public Set<Tag> getTags() {
    return tags;
  }

  public Outcome getOutcome() {
    return outcome;
  }

  public Duration getTotalEstimatedTime() {
    Duration duration = estimatedTime;
    for (Task task : getChildren()) {
      Duration childDuration = task.getTotalEstimatedTime();
      if (childDuration != null) {
        if (duration == null) {
          duration = childDuration;
        } else {
          duration = duration.plus(childDuration);
        }
      }
    }
    return duration;
  }

  public Duration getTotalWorkDuration() {
    if (getWorkUnits().isEmpty()) {
      return Duration.ofMillis(0);
    }
    Duration totalTime = getWorkUnits().stream().reduce(Duration.ofMillis(0), (duration, workunit) -> workunit.getDuration().plus(duration), (dur1, dur2) -> dur1.plus(dur2));
    Duration took;
    if (getWorkUnits().last().getEnd() == null) {
      LocalDateTime start = getWorkUnits().last().getStart();
      Duration lastDuration = Duration.between(start, LocalDateTime.now());
      took = totalTime.plus(lastDuration);
    } else {
      took = totalTime;
    }
    return took;
  }

  public Schedule getSchedule() {
    return schedule;
  }

  public Task setSchedule(Schedule schedule) {
    this.schedule = schedule;
    return this;
  }

  public Set<Information> getInformations() {
    return informations;
  }

  @Override
  public String toString() {
    return "Task [name=" + name + ", finishTime=" + finishTime + "]";
  }
}