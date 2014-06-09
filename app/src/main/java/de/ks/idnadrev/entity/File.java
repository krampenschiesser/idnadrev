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

import com.google.common.io.Files;
import de.ks.persistence.entity.NamedPersistentObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.IOException;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

@Entity
public class File extends NamedPersistentObject<File> {
  private static final Logger log = LoggerFactory.getLogger(File.class);
  private static final long serialVersionUID = 1L;

  @ManyToOne
  protected Note note;
  @ManyToOne
  protected Thought thought;

  @Lob
  @Basic(fetch = FetchType.LAZY)
  protected byte[] data;

  public File() {
    //
  }

  public Thought getThought() {
    return thought;
  }

  public void setThought(Thought thought) {
    this.thought = thought;
  }

  public File(String name) {
    super(name);
  }

  public Note getNote() {
    return note;
  }

  protected void setNote(Note note) {
    this.note = note;
  }

  public byte[] getData() {
    return data;
  }

  public File setData(byte[] data) {
    this.data = data;
    return this;
  }

  public static File fromFile(java.io.File file) throws IOException {
    try {
      byte[] data = Files.toByteArray(file);
      File noteFile = new File(file.getName()).setData(data);
      return noteFile;
    } catch (IllegalArgumentException e) {
      log.error("File {0} is too big");
      throw e;
    }
  }
}

