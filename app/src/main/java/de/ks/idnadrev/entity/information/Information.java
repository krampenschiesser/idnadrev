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

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.ks.flatadocdb.annotation.Child;
import de.ks.flatadocdb.annotation.Entity;
import de.ks.flatadocdb.annotation.ToMany;
import de.ks.flatadocdb.annotation.lifecycle.PostRemove;
import de.ks.flatadocdb.annotation.lifecycle.PostUpdate;
import de.ks.flatadocdb.defaults.SingleFolderGenerator;
import de.ks.flatadocdb.session.EntityDelete;
import de.ks.idnadrev.entity.*;
import de.ks.idnadrev.entity.adoc.AdocFile;
import de.ks.idnadrev.entity.adoc.AdocFileNameGenerator;
import de.ks.idnadrev.entity.adoc.SameFolderGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Entity(folderGenerator = SingleFolderGenerator.class, luceneDocExtractor = AdocContainerLuceneExtractor.class)
public class Information extends TaggedEntity implements FileContainer<Information> {
  private static final Logger log = LoggerFactory.getLogger(Information.class);

  @JsonIgnore
  protected Set<Path> files = new HashSet<>();

  @Child(fileGenerator = AdocFileNameGenerator.class, folderGenerator = SameFolderGenerator.class, lazy = false)
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
    if (files.isEmpty() && getPathInRepository() != null) {
      FilenameFilter filenameFilter = getFileFilter();
      File[] files = getPathInRepository().getParent().toFile().listFiles(filenameFilter);
      if (files != null) {
        Arrays.asList(files).stream().map(File::toPath).forEach(this.files::add);
      }
    }
    return files;
  }

  protected FilenameFilter getFileFilter() {
    String ownFileName = getPathInRepository().getFileName().toString();
    String adocFileName = StringUtils.replace(ownFileName, ".json", ".adoc");
    return (dir, fileName) -> !fileName.equals(ownFileName) && !fileName.equals(adocFileName) && !fileName.endsWith(EntityDelete.DELETION_SUFFIX);
  }

  @PostUpdate
  public void copyFiles() {
    Path parent = getPathInRepository().getParent();

    files.stream().filter(file -> !file.getParent().equals(parent)).forEach(file -> {
      try {
        Files.copy(file, parent.resolve(file.getFileName()));
        log.debug("For Information {} copied {} to {}", getName(), file, parent);
      } catch (IOException e) {
        log.error("Could not copy requested file {} to {}", file, parent, e);
      }
    });
  }

  @PostRemove
  public void cleanup() {
    for (Path path : getFiles()) {
      try {
        Files.deleteIfExists(path);
      } catch (IOException e) {
        log.error("Could not remove {}", path, e);
      }
    }
  }
}