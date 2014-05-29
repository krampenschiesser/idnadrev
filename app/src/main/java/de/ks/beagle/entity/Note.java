/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */
@Entity
public class Note extends NamedPersistentObject<Note> implements FileContainer {
  public static final String NOTE_TAG_JOINTABLE = "note_tag";

  private static final long serialVersionUID = 1L;

  @Basic(fetch = FetchType.LAZY)
  @Lob
  protected String content;

  @ManyToOne
  protected Task task;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "note")
  protected Set<File> files = new HashSet<>();

  @ManyToMany(cascade = CascadeType.PERSIST)
  @JoinTable(name = NOTE_TAG_JOINTABLE)
  protected Set<Tag> tags = new HashSet<>();

  @ManyToOne
  protected Category category;

  public Note() {
    //
  }

  public Note(String name) {
    super(name);
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }


  public Task getTask() {
    return task;
  }

  public void setTask(Task task) {
    this.task = task;
  }

  public Note addTag(Tag tag) {
    getTags().add(tag);
    return this;
  }

  public Set<Tag> getTags() {
    return tags;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public Set<File> getFiles() {
    return files;
  }

  public void addFile(File file) {
    this.files.add(file);
    file.setNote(this);
  }

  @Override
  public void removeFile(File file) {
    this.files.remove(file);
    file.setNote(null);
  }

}