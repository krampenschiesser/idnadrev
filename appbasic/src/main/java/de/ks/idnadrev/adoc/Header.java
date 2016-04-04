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

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Set;

public class Header {
  public static final String AUTHOR_LINE = "authorLine";
  public static final String REVISION_LINE = "revLine";
  public static final String TITLE = "title";
  protected Set<String> tags;
  protected LocalDateTime fileTime;
  protected LocalDateTime revDate;
  protected LinkedHashMap<String, String> headerElements = new LinkedHashMap<>();
  protected int firstLine, lastLine;

  public Set<String> getTags() {
    return tags;
  }

  public Header setTags(Set<String> tags) {
    this.tags = tags;
    return this;
  }

  public LocalDateTime getFileTime() {
    return fileTime;
  }

  public Header setFileTime(LocalDateTime fileTime) {
    this.fileTime = fileTime;
    return this;
  }

  public LocalDateTime getRevDate() {
    return revDate;
  }

  public Header setRevDate(LocalDateTime revDate) {
    this.revDate = revDate;
    return this;
  }

  public String getAuthor() {
    return getHeaderElement("author");
  }

  public String getTitle() {
    return getHeaderElement("title");
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
      b.append("= ").append(headerElements.get(TITLE)).append("\n");
    }
    if (headerElements.containsKey(AUTHOR_LINE)) {
      b.append(headerElements.get(AUTHOR_LINE)).append("\n");
    }
    if (headerElements.containsKey(REVISION_LINE)) {
      b.append(headerElements.get(REVISION_LINE)).append("\n");
    }
    LinkedHashMap<String, String> copy = new LinkedHashMap<>(headerElements);
    copy.remove(TITLE);
    copy.remove(AUTHOR_LINE);
    copy.remove(REVISION_LINE);
    copy.forEach((option, value) -> b.append(":").append(option).append(": ").append(value).append("\n"));
    return b.toString();
  }
}
