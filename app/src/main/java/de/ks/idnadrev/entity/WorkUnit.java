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

import de.ks.persistence.converter.LocalDateTimeConverter;
import de.ks.persistence.entity.AbstractPersistentObject;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * <pre>
 * Represents a single period spend on working at a task without any interruptions.
 * Multiple WorkUnits on a Task are an indicator for
 * a) the task being too long
 * b) you are not able to work without interruptions in your environment
 * </pre>
 */
@Entity
public class WorkUnit extends AbstractPersistentObject<WorkUnit> implements Comparable<WorkUnit> {
  private static final long serialVersionUID = 1L;

  @Column(columnDefinition = "TIMESTAMP")
  @Convert(converter = LocalDateTimeConverter.class)
  protected LocalDateTime start;
  @Column(columnDefinition = "TIMESTAMP")
  @Convert(converter = LocalDateTimeConverter.class)
  protected LocalDateTime end;

  @ManyToOne(optional = false)
  protected Task task;

  protected WorkUnit() {
    //
  }

  public WorkUnit(Task task) {
    this.task = task;
    start = LocalDateTime.now();
    task.getWorkUnits().add(this);
  }

  public void setStart(LocalDateTime start) {
    this.start = start;
  }

  public void stop() {
    if (end == null) {
      end = LocalDateTime.now();
    }
  }

  public LocalDateTime getStart() {
    return start;
  }

  public LocalDateTime getEnd() {
    return end;
  }

  public long getSpentMinutes() {
    return getDuration().toMinutes();
  }

  public Duration getDuration() {
    if (getEnd() == null) {
      return Duration.ofMillis(0);
    } else {
      return Duration.between(getStart(), getEnd());
    }
  }

  public long getSpentMillis() {
    return getDuration().toMillis();
  }

  @Override
  public int compareTo(WorkUnit o) {
    return this.start.compareTo(o.start);
  }

  public Task getTask() {
    return task;
  }

  public void setTask(Task task) {
    this.task = task;
  }

  public void setEnd(LocalDateTime time) {
    this.end = time;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof WorkUnit)) {
      return false;
    }

    WorkUnit workUnit = (WorkUnit) o;

    if (start != null ? !start.equals(workUnit.start) : workUnit.start != null) {
      return false;
    }
    if (task != null ? !task.equals(workUnit.task) : workUnit.task != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = start != null ? start.hashCode() : 0;
    result = 31 * result + (task != null ? task.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("WorkUnit{");
    sb.append("start=").append(start);
    sb.append(", end=").append(end);
    sb.append(", task=").append(task);
    sb.append('}');
    return sb.toString();
  }
}