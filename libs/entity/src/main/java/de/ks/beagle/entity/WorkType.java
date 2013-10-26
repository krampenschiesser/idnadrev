package de.ks.beagle.entity;

import de.ks.persistence.entity.NamedPersistentObject;

import javax.persistence.Entity;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */
@Entity
public class WorkType extends NamedPersistentObject<WorkType> {
  private static final long serialVersionUID = 1L;

  protected String description;

  public WorkType() {
  }

  public WorkType(String name) {
    super(name);
  }

  public String getDescription() {
    return description;
  }

  public WorkType setDescription(String description) {
    this.description = description;
    return this;
  }
}
