package de.ks.beagle.entity;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

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

  @Column(columnDefinition = "VARCHAR(250)")
  @Convert(converter = LocalDateTimeConverter.class)
  protected LocalDateTime start;
  @Column(columnDefinition = "VARCHAR(250)")
  @Convert(converter = LocalDateTimeConverter.class)
  protected LocalDateTime end;

  @ManyToOne
  protected Task task;

  protected WorkUnit() {
    //
  }

  public WorkUnit(Task task) {
    this.task = task;
    start = LocalDateTime.now();
  }

  public void stop() {
    end = LocalDateTime.now();
  }

  public LocalDateTime getStart() {
    return start;
  }

  public LocalDateTime getEnd() {
    return end;
  }

  public int getSpentMinutes() {
    long millis = getSpentMillis();
    return (int) (millis / 1000 / 60);
  }

  public Duration getDuration() {
    return Duration.between(getStart(), getEnd());
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

  protected void setEnd(LocalDateTime time) {
    this.end = time;
  }
}