package de.ks.beagle.entity;

import de.ks.persistence.entity.NamedPersistentObject;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */
@Entity
public class Thought extends NamedPersistentObject<Thought> {
  private static final long serialVersionUID = 1L;

  @Column(length = 4096)
  protected String description;

  public Thought() {
  }

  public Thought(String name) {
    super(name);
  }

  public Thought setDescription(String description) {
    this.description = description;
    return this;
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

}
