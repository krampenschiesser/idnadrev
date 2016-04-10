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
package de.ks.idnadrev.repository;

import de.ks.idnadrev.adoc.AdocFileParser;
import de.ks.idnadrev.index.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Scanner {
  private static final Logger log = LoggerFactory.getLogger(Scanner.class);

  public static final String FILE_EXTENSION_ADOC = ".adoc";
  public static final String FILE_EXTENSION_ASCCIDOC = ".asciidoc";
  public static final String FILE_EXTENSION_ASC = ".asc";

  final AdocFileParser parser;
  final Index index;
  final ExecutorService executorService;

  @Inject
  public Scanner(AdocFileParser parser, Index index, ExecutorService executorService) {
    this.parser = parser;
    this.index = index;
    this.executorService = executorService;
  }

  public void scan(Repository repository, @Nullable ProgressCallback progress) {
    Path path = repository.getPath();

    HashSet<Path> adocFiles = new HashSet<>();

    getFiles(path, adocFiles);
    if (progress != null) {
      progress.progress(0, adocFiles.size());
    }
    AtomicInteger count = new AtomicInteger();
    List<CompletableFuture<Void>> futures = adocFiles.stream().map(p -> CompletableFuture.supplyAsync(() -> parser.parse(p, repository), executorService)//
      .thenAccept(adocFile -> {
        index.add(adocFile);
        repository.addAdocFile(adocFile);

        count.incrementAndGet();
        if (progress != null) {
          progress.progress(count.get(), adocFiles.size());
        }
      })).collect(Collectors.toList());

    futures.forEach(CompletableFuture::join);
  }

  protected void getFiles(Path path, final HashSet<Path> adocFiles) {
    try {
      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          String fileName = file.getFileName().toString().toLowerCase(Locale.ROOT);
          if (fileName.endsWith(FILE_EXTENSION_ADOC) || fileName.endsWith(FILE_EXTENSION_ASCCIDOC) || fileName.endsWith(FILE_EXTENSION_ASC)) {
            adocFiles.add(file);
          }
          return super.visitFile(file, attrs);
        }
      });
    } catch (IOException e) {
      log.error("Could not parse repository {}", path, e);
    }
  }
}
