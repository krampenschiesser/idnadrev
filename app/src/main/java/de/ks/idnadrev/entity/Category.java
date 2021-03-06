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
import javafx.scene.paint.Color;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Entity
public class Category extends NamedPersistentObject<Category> {
  private static final long serialVersionUID = 1L;

  private static final List<String> defaultColors = Arrays.asList("#f3622d", "#fba71b", "#57b757", "#41a9c9", "#4258c9", "#9a42c8", "#c84164", "#888888");
  @ManyToOne
  protected Category parent;

  @OneToMany(mappedBy = "parent")
  protected Set<Category> children = new HashSet<>();

  @ManyToOne(cascade = CascadeType.ALL)
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
    setColorAsString(defaultColors.get(nextColor));
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

  public String getColorAsString() {
    return color;
  }

  public Category setColorAsString(String color) {
    this.color = color;
    return this;
  }

  public Category setColor(Color clr) {
    this.color = String.format("#%02X%02X%02X", (int) (clr.getRed() * 255), (int) (clr.getGreen() * 255), (int) (clr.getBlue() * 255));
    return this;
  }

  public Color getColor() {
    return Color.web(color);
  }

  public FileReference getImage() {
    return image;
  }

  public Category setImage(FileReference image) {
    this.image = image;
    return this;
  }
}
