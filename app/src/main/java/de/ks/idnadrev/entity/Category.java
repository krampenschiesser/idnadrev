/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
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

package de.ks.idnadrev.entity;

import de.ks.persistence.entity.NamedPersistentObject;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */
@Entity
public class Category extends NamedPersistentObject<Category> {
  private static final List<String> defaultColors = Arrays.asList("#f3622d", "#fba71b", "#57b757", "#41a9c92", "#4258c9", "#9a42c8", "#c84164", "#888888");
  @ManyToOne
  protected Category parent;

  @OneToMany(mappedBy = "parent")
  protected Set<Category> children = new HashSet<>();

  @ManyToOne
  protected FileReference image;

  protected String color;

  protected Category() {
    super();
    fillDefaultColor();
  }

  public Category(String name) {
    super(name);
    fillDefaultColor();
  }

  protected void fillDefaultColor() {
    int nextColor = ThreadLocalRandom.current().nextInt(0, defaultColors.size());
    setColor(defaultColors.get(nextColor));
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

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public FileReference getImage() {
    return image;
  }
}
