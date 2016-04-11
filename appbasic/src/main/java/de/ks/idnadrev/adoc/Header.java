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

import de.ks.idnadrev.repository.Repository;
import de.ks.idnadrev.util.GenericDateTimeParser;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class Header {
  public static final String AUTHOR_LINE = "authorLine";
  public static final String REVISION_LINE = "revLine";
  public static final String TITLE = "title";
  public static final String TYPE = "kstype";
  protected Set<String> tags = new HashSet<>();
  protected LocalDateTime fileTime;
  protected LocalDateTime revDate;
  protected LinkedHashMap<String, String> headerElements = new LinkedHashMap<>();
  protected int firstLine, lastLine;
  protected Repository repository;

  public Header(Repository repository) {
    this.repository = repository;
  }

  public Set<String> getTags() {
    return tags;
  }

  public String getTagString() {
    return tags.stream().collect(Collectors.joining(", "));
  }

  public Header setTagString(String tagString) {
    this.tags.clear();
    setHeaderElement("keywords", tagString);

    String[] split = StringUtils.split(tagString, ",");
    Arrays.asList(split).forEach(s -> tags.add(s.trim()));
    return this;
  }

  public Header setTags(Set<String> tags) {
    boolean wasEmpty = this.tags.isEmpty();
    this.tags = tags;
    if (!wasEmpty) {
      setHeaderElement("keywords", tags.stream().collect(Collectors.joining(", ")));
    }
    return this;
  }

  public LocalDateTime getFileTime() {
    return fileTime;
  }

  Header setFileTime(LocalDateTime fileTime) {
    this.fileTime = fileTime;
    return this;
  }

  public LocalDateTime getRevDate() {
    return revDate;
  }

  Header setRevDate(LocalDateTime revDate) {
    this.revDate = revDate;
    return this;
  }

  public Header setRevDate(LocalDateTime revDate, GenericDateTimeParser dateTimeParser) {
    this.revDate = revDate;
    setHeaderElement("revdate", dateTimeParser.parse(revDate, repository));
    return this;
  }

  public String getAuthor() {
    return getHeaderElement("author");
  }

  public String getTitle() {
    return getHeaderElement("title");
  }

  public Header setTitle(String title) {
    setHeaderElement("title", title);
    return this;
  }

  public void setHeaderElement(String key, String value) {
    if (headerElements.containsKey(key)) {
      headerElements.replace(key, value);
    } else {
      headerElements.put(key, value);
    }
  }

  public void setHeaderElements(LinkedHashMap<String, String> headerElements) {
    this.headerElements.putAll(headerElements);
  }

  public String getHeaderElement(String key) {
    return headerElements.get(key);
  }

  public int getLastLine() {
    return lastLine;
  }

  public Header setLastLine(int lastLine) {
    this.lastLine = lastLine;
    return this;
  }

  public int getFirstLine() {
    return firstLine;
  }

  public Header setFirstLine(int firstLine) {
    this.firstLine = firstLine;
    return this;
  }

  public String writeBack() {
    StringBuilder b = new StringBuilder();
    if (headerElements.containsKey(TITLE)) {
      b.append("= ").append(headerElements.get(TITLE)).append(AdocFile.newLine);
    }
    if (headerElements.containsKey(AUTHOR_LINE)) {
      b.append(headerElements.get(AUTHOR_LINE)).append(AdocFile.newLine);
    }
    if (headerElements.containsKey(REVISION_LINE)) {
      b.append(headerElements.get(REVISION_LINE)).append(AdocFile.newLine);
    }
    LinkedHashMap<String, String> copy = new LinkedHashMap<>(headerElements);
    copy.remove(TITLE);
    copy.remove(AUTHOR_LINE);
    copy.remove(REVISION_LINE);
    copy.forEach((option, value) -> b.append(":").append(option).append(": ").append(value).append(AdocFile.newLine));
    return b.toString();
  }

  public boolean isTask() {
    return "task".equals(getHeaderElement(TYPE));
  }
}
