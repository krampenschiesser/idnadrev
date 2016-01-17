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

import de.ks.flatadocdb.annotation.Child;
import de.ks.flatadocdb.annotation.Entity;
import de.ks.flatadocdb.annotation.ToMany;
import de.ks.idnadrev.entity.*;
import de.ks.idnadrev.entity.adoc.AdocFile;
import de.ks.idnadrev.entity.adoc.AdocFileNameGenerator;
import de.ks.idnadrev.entity.adoc.SameFolderGenerator;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Entity(luceneDocExtractor = AdocContainerLuceneExtractor.class)
public class Information extends TaggedEntity implements FileContainer<Information> {
  protected Set<Path> files = new HashSet<>();

  @Child(fileGenerator = AdocFileNameGenerator.class, folderGenerator = SameFolderGenerator.class)
  protected AdocFile adocFile;

  @ToMany
  protected Set<Task> task;

  protected Information() {
    super(null);
  }

  public Information(String name) {
    super(name);
  }

  @Override
  public void setName(String name) {
    super.setName(name);
    if (adocFile == null) {
      adocFile = new AdocFile(name);
    } else {
      adocFile.setName(name);
    }
  }

  public String getContent() {
    return adocFile == null ? null : adocFile.getContent();
  }

  public Information setContent(String content) {
    if (adocFile == null) {
      adocFile = new AdocFile(getName()).setContent(content);
    } else {
      adocFile.setContent(content);
    }
    return this;
  }

  public AdocFile getAdocFile() {
    return adocFile;
  }

  @Override
  public Set<Tag> getTags() {
    return tags;
  }

  @Override
  public Set<Path> getFiles() {
    return files;
  }
}