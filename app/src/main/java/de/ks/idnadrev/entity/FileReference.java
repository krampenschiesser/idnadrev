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

import de.ks.idnadrev.entity.validation.OwnerFilled;
import de.ks.persistence.entity.AbstractPersistentObject;
import de.ks.persistence.entity.NamedPersistentObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

@Entity
@OwnerFilled
public class FileReference extends NamedPersistentObject<FileReference> {//TODO use file storage path and md5 checksum
  private static final Logger log = LoggerFactory.getLogger(FileReference.class);
  private static final long serialVersionUID = 1L;

  @ManyToOne(cascade = CascadeType.ALL)
  protected Note note;
  @ManyToOne(cascade = CascadeType.ALL)
  protected Thought thought;
  @ManyToOne(cascade = CascadeType.ALL)
  protected Task task;

  protected String md5Sum;
  @NotNull
  protected String fileStorePath;

  public FileReference() {
    //
  }

  public FileReference(String name, String md5) {
    super(name);
    md5Sum = md5;
  }

  public Thought getThought() {
    return thought;
  }

  public void setThought(Thought thought) {
    this.thought = thought;
    this.task = null;
    this.note = null;
  }

  public Note getNote() {
    return note;
  }

  protected void setNote(Note note) {
    this.note = note;
    this.task = null;
    this.thought = null;
  }

  public Task getTask() {
    return task;
  }

  public void setTask(Task task) {
    this.task = task;
    this.thought = null;
    this.note = null;
  }

  public String getFileStorePath() {
    return fileStorePath;
  }

  public FileReference setFileStorePath(String fileStorePath) {
    this.fileStorePath = fileStorePath;
    return this;
  }

  public String getMd5Sum() {
    return md5Sum;
  }

  public void setMd5Sum(String md5Sum) {
    this.md5Sum = md5Sum;
  }

  public AbstractPersistentObject getOwner() {
    if (getTask() != null) {
      return getTask();
    } else if (getThought() != null) {
      return getThought();
    } else if (getNote() != null) {
      return getNote();
    }
    return null;
  }
}

