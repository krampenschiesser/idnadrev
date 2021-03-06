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

package de.ks.idnadrev.entity.information;

import de.ks.idnadrev.entity.*;
import de.ks.persistence.entity.NamedPersistentObject;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Information<E extends Information<E>> extends NamedPersistentObject<E> implements Tagged, Categorized {
  public static final String INFORMATION_TAG_JOINTABLE = "information_tag";

  private static final long serialVersionUID = 1L;

  @Basic(fetch = FetchType.LAZY)
  @Lob
  protected String content;

  @ManyToOne
  protected Task task;

  @ManyToMany(cascade = CascadeType.PERSIST)
  protected Set<Tag> tags = new HashSet<>();

  @ManyToOne
  protected Category category;

  protected Information() {
    //
  }

  public Information(String name) {
    super(name);
  }

  public String getContent() {
    return content;
  }

  @SuppressWarnings("unchecked")
  public E setContent(String content) {
    this.content = content;
    return (E) this;
  }

  @Override
  public Set<Tag> getTags() {
    return tags;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public Task getTask() {
    return task;
  }

  public void setTask(Task task) {
    this.task = task;
  }
}