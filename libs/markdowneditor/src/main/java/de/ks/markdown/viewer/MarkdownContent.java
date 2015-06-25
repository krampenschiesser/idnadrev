/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.markdown.viewer;

import java.io.File;

public class MarkdownContent {
  protected final String identifier;
  protected final String markdown;
  protected final File markdownFile;

  public MarkdownContent(String identifier, String markdown) {
    this.identifier = identifier;
    this.markdown = markdown;
    markdownFile = null;
  }

  public MarkdownContent(String identifier, File markdownFile) {
    this.identifier = identifier;
    this.markdownFile = markdownFile;
    this.markdown = null;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getMarkdown() {
    return markdown;
  }

  public File getMarkdownFile() {
    return markdownFile;
  }

  public boolean hasFile() {
    return markdownFile != null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MarkdownContent)) {
      return false;
    }

    MarkdownContent that = (MarkdownContent) o;

    if (!identifier.equals(that.identifier)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return identifier.hashCode();
  }

  @Override
  public String toString() {
    return "MarkdownContent{" +
      "identifier='" + identifier + '\'' +
      ", markdown='" + markdown + '\'' +
      '}';
  }

}
