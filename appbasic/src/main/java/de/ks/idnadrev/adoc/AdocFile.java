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
package de.ks.idnadrev.adoc;

import com.google.common.base.StandardSystemProperty;
import de.ks.idnadrev.repository.Repository;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Asciidoctor file
 */
public class AdocFile {
  public static final String newLine = StandardSystemProperty.LINE_SEPARATOR.value();

  protected Repository repository;
  protected Path path;
  protected Set<CompanionFile> files;
  protected Header header;
  protected List<String> lines = new ArrayList<>();
  protected String fileName;

  public AdocFile(Path path, Repository repository, Header header) {
    this.repository = repository;
    this.path = path;
    this.header = header;
    if (path != null && path.getFileName() != null) {
      this.fileName = path.getFileName().toString();
    }
  }

  public String getFileName() {
    return fileName;
  }

  public AdocFile setFileName(String fileName) {
    this.fileName = fileName;
    return this;
  }

  public Set<CompanionFile> getFiles() {
    return files;
  }

  public AdocFile setFiles(Set<CompanionFile> files) {
    this.files = files;
    return this;
  }

  public Header getHeader() {
    return header;
  }

  public String getTitle() {
    return header.getTitle();
  }

  public AdocFile setTitle(String title) {
    header.setTitle(title);
    return this;
  }

  public void setLines(List<String> lines) {
    this.lines = lines;
  }

  public List<String> getLines() {
    return lines;
  }

  public void setContent(String content) {
    lines = Arrays.asList(StringUtils.splitPreserveAllTokens(content, newLine));
  }

  public String getContent() {
    return lines.stream().collect(Collectors.joining(newLine));
  }

  public Path getPath() {
    return path;
  }

  public AdocFile setPath(Path path) {
    this.path = path;
    return this;
  }

  public Repository getRepository() {
    return repository;
  }

  public AdocFile setRepository(Repository repository) {
    this.repository = repository;
    return this;
  }

  public String writeBack() {
    StringBuilder b = new StringBuilder();
    b.append(header.writeBack());
    if (!lines.isEmpty()) {
      b.append(newLine);
      b.append(getContent());
    }
    return b.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AdocFile)) {
      return false;
    }
    AdocFile adocFile = (AdocFile) o;
    return path.equals(adocFile.path);
  }

  @Override
  public int hashCode() {
    return path.hashCode();
  }
}
