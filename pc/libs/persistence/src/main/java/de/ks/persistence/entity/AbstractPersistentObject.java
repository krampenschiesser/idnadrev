package de.ks.persistence.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.io.Serializable;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */
@MappedSuperclass
public abstract class AbstractPersistentObject<T extends AbstractPersistentObject<T>> implements Serializable {
  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue
  protected Long id;
  @Version
  protected Long version;

  protected AbstractPersistentObject() {

  }

  public Long getId() {
    return id;
  }

  public Long getVersion() {
    return version;
  }
}
