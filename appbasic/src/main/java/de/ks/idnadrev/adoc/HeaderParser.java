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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@ThreadSafe
public class HeaderParser {
  private static final Logger log = LoggerFactory.getLogger(HeaderParser.class);
  public static final int MAX_HEADER_LINES = 50;
  private final GenericDateTimeParser parser;

  @Inject
  public HeaderParser(GenericDateTimeParser parser) {
    this.parser = parser;
  }

  public ParseResult parse(List<String> lines, Path path, Repository repository) {
    Header header = new Header(repository);
    header.setFileTime(getLastModifiedTime(path));

    int titleIdx = getTitleIndex(lines);
    if (titleIdx < 0) {
      return new ParseResult("No title found");
    }
    int endIdx = getEndIndex(lines, titleIdx);
    header.setFirstLine(titleIdx).setLastLine(endIdx);

    LinkedHashMap<String, String> headerElements = parseHeader(lines, titleIdx, endIdx);
    header.setTags(parseKeywords(headerElements.get("keywords")));

    if (!headerElements.containsKey("revdate") && headerElements.containsKey(Header.REVISION_LINE)) {
      String line = headerElements.get(Header.REVISION_LINE);
      int revDateStart = line.indexOf(", ");
      int revDateEnd = line.indexOf(": ");
      if (revDateEnd > 0 && revDateStart > 0) {
        String revDate = line.substring(revDateStart + 2, revDateEnd);
        header.setRevDate(parser.parse(revDate, repository));
      }
    } else if (headerElements.containsKey("revdate")) {
      header.setRevDate(parser.parse(headerElements.get("revdate"), repository));
    }
    header.setHeaderElements(headerElements);
    return new ParseResult(header);
  }

  private LinkedHashMap<String, String> parseHeader(List<String> lines, int titleIdx, int endIdx) {
    LinkedList<Pair> list = new LinkedList<>();

    for (int i = titleIdx; i < endIdx; i++) {
      String line = lines.get(i).trim();
      if (line.startsWith("= ")) {
        list.add(Pair.of(Header.TITLE, line.substring(2)));
      } else if (line.startsWith(":")) {
        int endOfOption = line.indexOf(":", 2);
        String optionName = line.substring(1, endOfOption);
        String value = line.substring(endOfOption + 1).trim();
        list.add(Pair.of(optionName, value));
      } else if (i == titleIdx + 1) {
        list.add(Pair.of(Header.AUTHOR_LINE, line));
      } else if (i == titleIdx + 2) {
        list.add(Pair.of(Header.REVISION_LINE, line));
      } else {
        Pair last = list.getLast();
        last.setValue(last.getValue() + "\n" + line);
      }
    }
    LinkedHashMap<String, String> retval = new LinkedHashMap<>();
    list.forEach(p -> retval.put(p.getKey(), p.getValue()));
    return retval;
  }

  private int getEndIndex(List<String> lines, int titleIdx) {
    int max = (lines.size() - titleIdx) < MAX_HEADER_LINES ? lines.size() : MAX_HEADER_LINES;
    for (int i = titleIdx; i < max; i++) {
      String line = lines.get(i);
      if (line.isEmpty()) {
        return i;
      }
    }
    return lines.size();
  }

  private int getTitleIndex(List<String> lines) {
    int idx = 0;
    for (String line : lines) {
      if (line.startsWith("= ")) {
        return idx;
      }
      if (idx > MAX_HEADER_LINES) {
        break;
      }
      idx++;
    }
    return -1;
  }

  protected LocalDateTime getLastModifiedTime(Path path) {
    if (path != null) {
      try {
        FileTime lastModifiedTime = Files.getLastModifiedTime(path);
        return LocalDateTime.ofInstant(lastModifiedTime.toInstant(), ZoneId.systemDefault());
      } catch (IOException e) {
        log.error("Could not get file modification time of {}.", path, e);
      }
    }
    return null;
  }

  private Set<String> parseKeywords(String input) {
    HashSet<String> retval = new HashSet<>();
    if (input != null) {
      for (String s : StringUtils.split(input, ",")) {
        retval.add(s.trim());
      }
    }
    return retval;
  }

  public static class ParseResult {

    final Header header;
    final String parseError;

    public ParseResult(Header header) {
      this.header = header;
      parseError = null;
    }

    public ParseResult(String parseError) {
      this.parseError = parseError;
      header = null;
    }

    public Header getHeader() {
      return header;
    }

    public String getParseError() {
      return parseError;
    }
  }

  static class Pair {
    static Pair of(String key, String value) {
      return new Pair().setKey(key).setValue(value);
    }

    String key, value;

    public String getKey() {
      return key;
    }

    public Pair setKey(String key) {
      this.key = key;
      return this;
    }

    public String getValue() {
      return value;
    }

    public Pair setValue(String value) {
      this.value = value;
      return this;
    }
  }
}
