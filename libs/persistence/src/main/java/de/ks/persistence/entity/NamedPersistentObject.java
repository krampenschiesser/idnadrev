package de.ks.persistence.entity;


import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

@MappedSuperclass
public abstract class NamedPersistentObject<T extends NamedPersistentObject<T>> extends AbstractPersistentObject<T> {
  private static final long serialVersionUID = 1L;
  @NotNull
  @Column(nullable = false, unique = true)
  protected String name;

  protected NamedPersistentObject() {

  }

  public NamedPersistentObject(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public T setName(String name) {
    this.name = name;
    return (T) this;
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
    if (!(obj instanceof NamedPersistentObject)) {
      return false;
    }
    NamedPersistentObject<?> other = (NamedPersistentObject<?>) obj;
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
