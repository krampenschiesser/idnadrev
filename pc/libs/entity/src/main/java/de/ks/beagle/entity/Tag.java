package de.ks.beagle.entity;

import de.ks.persistence.entity.NamedPersistentObject;

import javax.persistence.Entity;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */
@Entity
public class Tag extends NamedPersistentObject<Tag> {

  public Tag() {
    //
  }

  public Tag(String name) {
    super(name);
  }
}
