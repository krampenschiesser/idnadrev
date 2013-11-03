package de.ks.beagle.entity;

import de.ks.persistence.entity.NamedPersistentObject;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */
@Entity
public class Category extends NamedPersistentObject<Category> {

  @ManyToOne
  protected Category parent;

  @OneToMany(mappedBy = "pane")
  protected Set<Category> children = new HashSet<>();

  public Category() {
    super();
  }

  public Category(String name) {
    super(name);
  }

  public Set<Category> getChildren() {
    return children;
  }

  public void addChild(Category child) {
    children.add(child);
    child.setParent(this);
  }

  public Category getParent() {
    return parent;
  }

  public void setParent(Category parent) {
    this.parent = parent;
  }
}
