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
package de.ks.file;

import com.google.common.base.Charsets;
import de.ks.LauncherRunner;
import de.ks.idnadrev.entity.FileReference;
import de.ks.idnadrev.entity.Thought;
import de.ks.option.Option;
import de.ks.option.Options;
import de.ks.persistence.PersistentWork;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class FilesTest {
  private static final Logger log = LoggerFactory.getLogger(FilesTest.class);
  private static final String TMPDIR = System.getProperty("java.io.tmpdir");

  @Inject
  FileStore fileStore;

  @Before
  public void setUp() throws Exception {
    PersistentWork.deleteAllOf(Thought.class, Option.class);
    String fileStoreDir = TMPDIR + File.separator + "idnadrevTestStore";
    Options.store(fileStoreDir, FileOptions.class).getFileStoreDir();
    File file = new File(fileStoreDir);
    if (file.exists()) {
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

  @Test
  public void testSaveFileWithThought() throws Exception {
    File file = createTmpFile();
    Thought bla = new Thought("bla");
    PersistentWork.persist(bla);

    FileReference fileReference = fileStore.createReference(bla, file).get();

    String expectedAbsolutePath = TMPDIR + File.separator + "idnadrevTestStore" + File.separator + Thought.class.getSimpleName() + File.separator + String.format("%09d", bla.getId()) + File.separator + file.getName();
    assertEquals(expectedAbsolutePath, fileReference.getAbsolutePath());
    assertNotNull(fileReference.getMd5Sum());
    assertEquals(fileReference.getMd5Sum(), DigestUtils.md5Hex("hello world"));
    log.info("Generated md5sum {}", fileReference.getMd5Sum());
    assertTrue(new File(expectedAbsolutePath).exists());
  }

  protected File createTmpFile() throws IOException {
    String tempFile = TMPDIR + File.separator + "input.txt";
    log.info("Creating tempfile {}", tempFile);
    File file = new File(tempFile);
    file.createNewFile();
    com.google.common.io.Files.write("hello world", file, Charsets.US_ASCII);
    return file;
  }

  @Test
  public void testMove() throws Exception {
    Options.store(false, FileOptions.class).shouldCopy();

    File file = createTmpFile();
    Thought bla = new Thought("bla");
    PersistentWork.persist(bla);

    FileReference fileReference = fileStore.createReference(bla, file).get();

    assertFalse(file.exists());
    assertTrue(new File(fileReference.getAbsolutePath()).exists());
  }
}
