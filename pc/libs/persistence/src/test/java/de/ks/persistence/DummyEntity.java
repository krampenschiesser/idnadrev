package de.ks.persistence;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 *
 */
@Entity
public class DummyEntity implements Serializable {
  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue
  protected Long id;
  @Version
  protected Long version;

  @NotNull
  protected String name;

  protected DummyEntity() {
    //
  }

  public DummyEntity(String name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public Long getVersion() {
    return version;
  }

  public String getName() {
    return name;
  }

  public DummyEntity setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof DummyEntity)) {
      return false;
    }
    DummyEntity other = (DummyEntity) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + ": [name=" + name + "]";
  }

}
