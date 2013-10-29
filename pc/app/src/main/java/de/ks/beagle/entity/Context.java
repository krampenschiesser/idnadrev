package de.ks.beagle.entity;

import de.ks.persistence.entity.NamedPersistentObject;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Context extends NamedPersistentObject<Context> {
  private static final long serialVersionUID = 1L;

  @OneToMany(mappedBy = "context", fetch = FetchType.LAZY)
  protected Set<Task> tasks = new HashSet<>();

  public Context() {
  }

  public Context(String name) {
    super(name);
  }

  public Set<Task> getTasks() {
    return tasks;
  }

}
