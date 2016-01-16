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
import de.ks.flatadocdb.defaults.SingleFolderGenerator;
import de.ks.flatadocdb.query.Query;
import de.ks.idnadrev.entity.adoc.AdocFile;
import de.ks.idnadrev.entity.adoc.AdocFileNameGenerator;
import de.ks.idnadrev.entity.adoc.SameFolderGenerator;
import de.ks.idnadrev.entity.information.TextInfo;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity(folderGenerator = SingleFolderGenerator.class, luceneDocExtractor = AdocContainerLuceneExtractor.class)
public class Task extends TaggedEntity implements FileContainer<Task> {
  @QueryProvider
  public static Query<Task, Boolean> finishedQuery() {
    return Query.of(Task.class, Task::isFinished);
  }

  @QueryProvider
  public static Query<Task, TaskState> stateQuery() {
    return Query.of(Task.class, Task::getState);
  }

  @QueryProvider
  public static Query<Task, String> contextNameQuery() {
    return Query.of(Task.class, t -> t.getContext().getName());
  }

  @Child(fileGenerator = AdocFileNameGenerator.class, folderGenerator = SameFolderGenerator.class, lazy = false)
  protected AdocFile adocFile;

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

  protected Set<Path> files = new HashSet<>();

  @ToMany
  protected Set<TextInfo> information = new HashSet<>();

  protected Task() {
    super(null);
  }

  public Task(String name) {
    super(name);
  }

  @Override
  public void setName(String name) {
    super.setName(name);
    if (adocFile == null) {
      adocFile = new AdocFile(name);
    } else {
      adocFile.setName(name);
    }
  }

  @Override
  public Set<Path> getFiles() {
    return files;
  }

  public AdocFile getAdocFile() {
    return adocFile;
  }

  public Task setDescription(String description) {
    if (adocFile == null) {
      adocFile = new AdocFile(getName()).setContent(description);
    } else {
      adocFile.setContent(description);
    }
    return this;
  }

  public String getDescription() {
    return adocFile == null ? null : adocFile.getContent();
  }

  public long getSpentMinutes() {
    Duration duration = Duration.ZERO;
    for (WorkUnit workUnit : getWorkUnits()) {
      duration = duration.plus(workUnit.getDuration());
    }
    return duration.toMinutes();
  }

  public WorkUnit start() {
    WorkUnit workUnit = new WorkUnit();
    getWorkUnits().add(workUnit);
    return workUnit;
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

  public Set<TextInfo> getInformation() {
    return information;
  }

  public Task addInformation(TextInfo info) {
    information.add(info);
    return this;
  }

  @Override
  public String toString() {
    return "Task [name=" + name + ", finishTime=" + finishTime + "]";
  }
}