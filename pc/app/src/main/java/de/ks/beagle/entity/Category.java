/*
 * Copyright [2014] [Christian Loehnert]
 *
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

  @OneToMany(mappedBy = "parent")
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
