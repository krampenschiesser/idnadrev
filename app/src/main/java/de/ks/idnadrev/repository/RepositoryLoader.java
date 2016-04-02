/*
 * Copyright [2015] [Christian Loehnert]
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
package de.ks.idnadrev.repository;

import com.google.common.base.StandardSystemProperty;
import de.ks.flatadocdb.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RepositoryLoader {
  private static final Logger log = LoggerFactory.getLogger(RepositoryLoader.class);
  private final Path path;

  public RepositoryLoader() {
    this(Paths.get(discoverDataDir(), "repositories.path"));
  }

  private static String discoverDataDir() {
    String workingDir = StandardSystemProperty.USER_DIR.value();
    log.info("##USERDIR={}", workingDir);
    Path dataDir = Paths.get(workingDir, "data");
    if (!Files.exists(dataDir)) {
      dataDir = Paths.get(workingDir).getParent().resolve("data");
    }
    if (!Files.exists(dataDir)) {
      throw new IllegalStateException("No data dir");
    } else {
      return dataDir.toString();
    }
  }

  public RepositoryLoader(Path path) {
    this.path = path;
  }

  public List<Repository> loadRepositories() {
    return loadRepositoryPaths().stream().map(Repository::new).collect(Collectors.toList());
  }

  public List<Path> loadRepositoryPaths() {
    try {
      if (Files.notExists(path)) {
        return Collections.emptyList();
      } else {
        List<String> lines = Files.readAllLines(path).stream().filter(line -> line != null && !line.trim().startsWith("#")).collect(Collectors.toList());
        return lines.stream().map(l -> Paths.get(l)).collect(Collectors.toList());
      }
    } catch (IOException e) {
      return Collections.emptyList();
    }
  }

  public void addRepository(Path repoPath) {
    addRepositories(Collections.singletonList(repoPath));
  }

  public void addRepositories(List<Path> repoPaths) {
    try {
      ArrayList<String> lines = new ArrayList<>();
      if (!Files.exists(path)) {
        Files.createDirectories(path.getParent());
      } else {
        lines.addAll(Files.readAllLines(path).stream().filter(line -> line != null).collect(Collectors.toList()));
      }

      for (Path repoPath : repoPaths) {
        if (Files.exists(repoPath)) {
          if (!lines.contains(repoPath.toString())) {
            lines.add(repoPath.toString());
          }
        }
      }
      Files.write(path, lines);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
