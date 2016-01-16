/*
 * Copyright [2016] [Christian Loehnert]
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
package de.ks.idnadrev.entity.adoc;

import de.ks.flatadocdb.annotation.Entity;
import de.ks.flatadocdb.annotation.Id;
import de.ks.flatadocdb.annotation.PathInRepository;
import de.ks.flatadocdb.annotation.Version;
import de.ks.flatadocdb.annotation.lifecycle.PostRemove;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(persister = AdocPersister.class, fileGenerator = AdocFileNameGenerator.class)
public class AdocFile {
  private static final Logger log = LoggerFactory.getLogger(AdocFile.class);
  public static final String TMP_SUFFIX = ".tmp.adoc";
  @Version
  protected long version;
  @Id
  protected String id;
  @PathInRepository
  protected Path pathInRepository;

  protected String name;
  protected LocalDateTime lastModified;
  protected String content;

  protected List<Path> includedFiles = new ArrayList<>();

  protected AdocFile() {
    this(null);
  }

  public AdocFile(String name) {
    this.name = name;
  }

  public long getVersion() {
    return version;
  }

  AdocFile setVersion(long version) {
    this.version = version;
    return this;
  }

  public String getId() {
    return id;
  }

  AdocFile setId(String id) {
    this.id = id;
    return this;
  }

  public Path getPathInRepository() {
    return pathInRepository;
  }

  /**
   * Used as source for rendering
   *
   * @return path
   */
  public Path getTmpFile() {
    String fileName = pathInRepository.getFileName().toString();
    fileName = StringUtils.replace(fileName, ".adoc", TMP_SUFFIX);
    return pathInRepository.getParent().resolve(fileName);
  }

  /**
   * Used as html rendering target
   *
   * @return path
   */
  public Path getRenderingPath() {
    String fileName = pathInRepository.getFileName().toString();
    fileName = fileName.substring(0, fileName.lastIndexOf('.')) + ".html";
    return pathInRepository.getParent().resolve(fileName);
  }

  AdocFile setPathInRepository(Path pathInRepository) {
    this.pathInRepository = pathInRepository;
    return this;
  }

  public String getName() {
    return name;
  }

  public AdocFile setName(String name) {
    this.name = name;
    return this;
  }

  public LocalDateTime getLastModified() {
    return lastModified;
  }

  public AdocFile setLastModified(LocalDateTime lastModified) {
    this.lastModified = lastModified;
    return this;
  }

  public String getContent() {
    return content == null ? "" : content;
  }

  public AdocFile setContent(String content) {
    this.content = content;
    return this;
  }

  void setIncludedFiles(List<Path> includedFiles) {
    this.includedFiles = includedFiles;
  }

  public List<Path> getIncludedFiles() {
    return includedFiles;
  }

  @PostRemove
  public void cleanup() {
    Path tmpFile = getTmpFile();
    Path renderingPath = getRenderingPath();
    try {
      if (Files.deleteIfExists(tmpFile)) {
        log.debug("Deleted {}", tmpFile);
      }
      if (Files.deleteIfExists(renderingPath)) {
        log.debug("Deleted {}", renderingPath);
      }
    } catch (IOException e) {
      log.error("Could nto remove tmpFile {}", e);
    }
  }
}
