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

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AdocFileParser {
  private final HeaderParser headerParser;

  @Inject
  public AdocFileParser(HeaderParser headerParser) {
    this.headerParser = headerParser;
  }

  public AdocFile parse(Path path, Repository repository) {
    try {
      Stream<Path> paths = Files.list(path.getParent());
      Set<CompanionFile> files = paths.filter(p -> p != path && !Files.isDirectory(p)).map(CompanionFile::new).collect(Collectors.toSet());
      List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
      return parse(path, repository, files, lines);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  public AdocFile parse(Path path, Repository repository, Set<CompanionFile> files, List<String> lines) {
    AdocFile adocFile = new AdocFile(path, repository);
    HeaderParser.ParseResult result = headerParser.parse(lines, path, repository);
    Header header = result.getHeader();
    String title = getTitle(lines);
    adocFile.setHeader(header);
    adocFile.setTitle(title);
    adocFile.setFiles(files);
    if (header.getLastLine() < lines.size()) {
      adocFile.setLines(lines.subList(header.getLastLine() + 1, lines.size()));
    }
    return adocFile;
  }

  protected String getTitle(List<String> lines) {
    String title = "NoTitle";
    int count = 0;
    for (String line : lines) {
      if (count > 5) {
        break;
      }
      if (line.startsWith("= ")) {
        title = line.substring(1).trim();
        break;
      }
      count++;
    }
    return title;
  }
}
