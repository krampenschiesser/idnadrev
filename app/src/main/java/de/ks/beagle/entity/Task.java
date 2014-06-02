/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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

package de.ks.beagle.entity;

import de.ks.persistence.converter.LocalDateConverter;
import de.ks.persistence.converter.LocalDateTimeConverter;
import de.ks.persistence.converter.LocalTimeConverter;
import de.ks.persistence.converter.PeriodConverter;
import de.ks.persistence.entity.NamedPersistentObject;
import de.ks.scheduler.Schedule;

import javax.persistence.*;
import java.time.*;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

@Entity
public class Task extends NamedPersistentObject<Task> {
  private static final long serialVersionUID = 1L;

  @Column(length = 4048)
  protected String description;

  @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true, mappedBy = "task")
  @OrderBy("start ASC")
  protected SortedSet<WorkUnit> workUnits = new TreeSet<>();

  //tracking
  @Column(columnDefinition = "VARCHAR(250)")
  @Convert(converter = LocalDateTimeConverter.class)
  protected LocalDateTime creationTime;
  @Column(columnDefinition = "VARCHAR(250)")
  @Convert(converter = LocalDateTimeConverter.class)
  protected LocalDateTime finishTime;


  @Column(columnDefinition = "VARCHAR(250)")
  @Convert(converter = PeriodConverter.class)
  protected Period estimatedTime;

  @Column(columnDefinition = "VARCHAR(250)")
  @Convert(converter = LocalDateConverter.class)
  protected LocalDate dueDate;//must be done before this date (and time)
  @Column(columnDefinition = "VARCHAR(250)")
  @Convert(converter = LocalTimeConverter.class)
  protected LocalTime dueTime;//if null, only date relevant

  @ManyToOne(targetEntity = Task.class)
  protected Schedule schedule;

  @OneToMany(cascade = {CascadeType.DETACH, CascadeType.PERSIST, CascadeType.REFRESH}, orphanRemoval = false, mappedBy = "task")
  protected Set<Note> notes = new HashSet<>();

  @ManyToOne
  protected Task parent;

  @OneToMany(mappedBy = "parent", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
  protected Set<Task> children = new HashSet<>();

  protected String delegationReason;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  protected Context context;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  protected WorkType workType;

  protected boolean project;

  protected Task() {

  }

  public Task(String name) {
    super(name);
    this.creationTime = LocalDateTime.now();
  }

  public Task(String name, String description) {
    this(name);
    this.description = description;
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

  public void setDescription(String description) {
    this.description = description;
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

  public void setFinished(boolean finished) {
    this.finishTime = LocalDateTime.now();
  }

  public boolean isFinished() {
    return finishTime != null;
  }

  public LocalDateTime getFinishTime() {
    return finishTime;
  }

  public LocalDateTime getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(LocalDateTime creationTime) {
    this.creationTime = creationTime;
  }

  public SortedSet<WorkUnit> getWorkUnits() {
    return workUnits;
  }

  public Task getParent() {
    return parent;
  }

  public void setParent(Task parent) {
    this.parent = parent;
  }

  public String getDelegationReason() {
    return delegationReason;
  }

  public void setDelegationReason(String delegationReason) {
    this.delegationReason = delegationReason;
  }

  public Context getContext() {
    return context;
  }

  public Task setContext(Context context) {
    this.context = context;
    return this;
  }

  public WorkType getWorkType() {
    return workType;
  }

  public Task setWorkType(WorkType workType) {
    this.workType = workType;
    return this;
  }

  public boolean isProject() {
    return project;
  }

  public Task setProject(boolean project) {
    this.project = project;
    return this;
  }

  public Task addNote(Note note) {
    this.getNotes().add(note);
    note.setTask(this);
    return this;
  }

  public Set<Note> getNotes() {
    return notes;
  }

  public Set<Task> getChildren() {
    return children;
  }

  public Task addChild(Task child) {
    getChildren().add(child);
    child.setParent(this);
    return this;
  }

  public Period getEstimatedTime() {
    return estimatedTime;
  }

  public void setEstimatedTime(Period estimatedTime) {
    this.estimatedTime = estimatedTime;
  }

  @Override
  public String toString() {
    return "Task [name=" + name + ", finishTime=" + finishTime + "]";
  }

}