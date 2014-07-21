/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
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

package de.ks.text;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.Assert.assertEquals;

public class AsciiDocMetaDataTest {
  private static final Logger log = LoggerFactory.getLogger(AsciiDocMetaDataTest.class);

  @Test
  public void testDiscoverDataDir() throws Exception {
    AsciiDocMetaData asciiDocMetaData = new AsciiDocMetaData();
    File file = asciiDocMetaData.disocverDataDir();
    log.info("Discovered {}", file);
  }

  @Test
  public void testUnzip() throws Exception {
    AsciiDocMetaData asciiDocMetaData = new AsciiDocMetaData();
    File dataDir = asciiDocMetaData.disocverDataDir();


    File mathjax = new File(dataDir, "mathjax");
    if (mathjax.exists()) {
      deleteDir(mathjax);
    }
    Files.deleteIfExists(new File(dataDir, "asciidoctor.css").toPath());
    Files.deleteIfExists(new File(dataDir, "asciidoctor-coderay.css").toPath());

    assertEquals(2, dataDir.listFiles().length);
    asciiDocMetaData.extract();
    assertEquals(5, dataDir.listFiles().length);
  }

  protected void deleteDir(File file) throws IOException {
    Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }
}