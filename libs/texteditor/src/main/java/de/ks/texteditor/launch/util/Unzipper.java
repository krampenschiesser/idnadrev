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

package de.ks.texteditor.launch.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Unzipper {
  private static final Logger log = LoggerFactory.getLogger(Unzipper.class);
  private final File zipFile;

  public Unzipper(File zipFile) {
    this.zipFile = zipFile;
  }

  public void unzip(File target) {
    try {
      Files.createDirectories(target.toPath());

      try (FileSystem fs = FileSystems.newFileSystem(zipFile.toPath(), null)) {
        log.info("Unzip from {}{} to {}", zipFile, fs.getRootDirectories().iterator().next(), target.toPath());

        Files.walkFileTree(fs.getRootDirectories().iterator().next(), new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            final Path destFile = Paths.get(target.toString(), file.toString());
            Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            final Path dirToCreate = Paths.get(target.toString(), dir.toString());
            if (Files.notExists(dirToCreate)) {
              Files.createDirectory(dirToCreate);
            }
            return FileVisitResult.CONTINUE;
          }
        });
      }

    } catch (IOException e) {
      log.error("unzip failed because of {}");
      throw new RuntimeException(e);
    }
  }
}
