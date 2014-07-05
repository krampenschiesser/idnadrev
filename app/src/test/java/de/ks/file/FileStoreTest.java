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
import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class FileStoreTest {
  private static final Logger log = LoggerFactory.getLogger(FileStoreTest.class);
  private static final String TMPDIR = System.getProperty("java.io.tmpdir");

  @Inject
  FileStore fileStore;
  private String fileStoreDir;
  private static final String md5 = DigestUtils.md5Hex("hello world");
  private static final String content = "hello world";

  @Before
  public void setUp() throws Exception {
    PersistentWork.deleteAllOf(FileReference.class, Thought.class, Option.class);
    fileStoreDir = TMPDIR + File.separator + "idnadrevTestStore";
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

  protected File createTmpFile() throws IOException {
    String tempFile = TMPDIR + File.separator + "input.txt";
    log.info("Creating tempfile {}", tempFile);
    File file = new File(tempFile);
    file.createNewFile();
    com.google.common.io.Files.write(content, file, Charsets.US_ASCII);
    return file;
  }

  @Test
  public void testSaveFileWithThought() throws Exception {
    File file = createTmpFile();
    Thought bla = new Thought("bla");
    PersistentWork.persist(bla);

    FileReference fileReference = fileStore.getReference(bla, file).get();

    assertThat(fileReference.getId(), greaterThan(0L));

    String expectedFileStorePath = Thought.class.getSimpleName() + File.separator + String.format("%09d", bla.getId()) + File.separator + file.getName();
    assertEquals(expectedFileStorePath, fileReference.getFileStorePath());
    assertNotNull(fileReference.getMd5Sum());
    assertEquals(fileReference.getMd5Sum(), md5);
    log.info("Generated md5sum {}", fileReference.getMd5Sum());
    assertTrue(new File(fileStoreDir + File.separator + expectedFileStorePath).exists());

    file = fileStore.getFile(fileReference);
    assertTrue(file.exists());
  }

  @Test
  public void testMove() throws Exception {
    Options.store(false, FileOptions.class).shouldCopy();

    File file = createTmpFile();
    Thought bla = new Thought("bla");
    PersistentWork.persist(bla);

    FileReference fileReference = fileStore.getReference(bla, file).get();

    assertFalse(file.exists());
    assertTrue(new File(fileStoreDir + File.separator + fileReference.getFileStorePath()).exists());
  }

  @Test
  public void testExistingFile() throws Exception {
    File file = createTmpFile();

    Thought bla = new Thought("bla");
    PersistentWork.persist(bla);
    FileReference fileReference = fileStore.getReference(bla, file).get();

    com.google.common.io.Files.write("hello sauerland", file, Charsets.US_ASCII);

    FileReference newReference = fileStore.getReference(bla, file).get();

    assertEquals(fileReference.getId(), newReference.getId());
    assertNotEquals(fileReference.getMd5Sum(), newReference.getMd5Sum());
    File fileStoreFile = fileStore.getFile(newReference);

    List<String> lines = com.google.common.io.Files.readLines(fileStoreFile, Charsets.US_ASCII);
    assertEquals(1, lines.size());
    assertEquals("hello sauerland", lines.get(0));
  }
}
