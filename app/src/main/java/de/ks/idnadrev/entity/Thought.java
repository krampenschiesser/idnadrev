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

import de.ks.entity.AdocFile;
import de.ks.entity.AdocFileNameGenerator;
import de.ks.entity.SameFolderGenerator;
import de.ks.flatadocdb.annotation.Child;
import de.ks.flatadocdb.annotation.Entity;
import de.ks.flatadocdb.entity.NamedEntity;
import de.ks.idnadrev.thought.ThoughtOptions;
import de.ks.standbein.option.Options;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */
@Entity
public class Thought extends NamedEntity implements FileContainer<Thought> {
  private static final long serialVersionUID = 1L;
  public static final String THOUGHT_FILE_JOINTABLE = "thought_file";

  @Child(fileGenerator = AdocFileNameGenerator.class, folderGenerator = SameFolderGenerator.class)
  protected AdocFile adocFile;
  protected LocalDate postponedDate;

  protected Set<FileReference> files = new HashSet<>();

  protected Thought() {
    super(null);
  }

  public Thought(String name) {
    super(name);
  }

  public AdocFile getAdocFile() {
    return adocFile;
  }

  public Thought setDescription(String description) {
    if (adocFile == null) {
      adocFile = new AdocFile(getName()).setContent(description);
    } else {
      adocFile.setContent(description);
    }
    return this;
  }

  public String getDescription() {
    return adocFile == null ? null : adocFile.getContent();
  }

  public String getShortDescription() {
    if ((getDescription() != null) && (getDescription().length() > 50)) {
      return getDescription().substring(0, 50);
    } else {
      return getDescription();
    }
  }

  public Set<FileReference> getFiles() {
    return files;
  }

  public void postPone(Options options) {
    ThoughtOptions thoughtOptions = options.get(ThoughtOptions.class);
    int daysToPostpone = thoughtOptions.getDaysToPostpone();
    postPone(daysToPostpone);
  }

  public void postPone(int daysToPostPone) {
    postponedDate = LocalDate.now().plusDays(daysToPostPone);
  }

  public boolean isPostponed() {
    if (postponedDate != null) {
      LocalDate now = LocalDate.now();
      return now.isBefore(postponedDate);
    }
    return false;
  }
}
