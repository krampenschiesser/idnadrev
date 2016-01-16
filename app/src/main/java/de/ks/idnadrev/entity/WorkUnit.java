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

import de.ks.flatadocdb.entity.BaseEntity;

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
//@Entity
public class WorkUnit extends BaseEntity implements Comparable<WorkUnit> {
  private static final long serialVersionUID = 1L;

  protected LocalDateTime start;
  protected LocalDateTime end;

  protected WorkUnit() {
    start = LocalDateTime.now();
  }

  public WorkUnit setStart(LocalDateTime start) {
    this.start = start;
    return this;
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

  public WorkUnit setEnd(LocalDateTime time) {
    this.end = time;
    return this;
  }

  public boolean isFinished() {
    return end != null;
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

    if (!start.equals(workUnit.start)) {
      return false;
    }
    return end.equals(workUnit.end);

  }

  @Override
  public int hashCode() {
    int result = start.hashCode();
    result = 31 * result + end.hashCode();
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("WorkUnit{");
    sb.append("start=").append(start);
    sb.append(", end=").append(end);
    sb.append('}');
    return sb.toString();
  }
}