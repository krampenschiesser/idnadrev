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
import com.google.common.net.MediaType;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class FileStoreTest {
  private static final Logger log = LoggerFactory.getLogger(FileStoreTest.class);
  private static final String TMPDIR = System.getProperty("java.io.tmpdir");

  @Inject
  FileStore fileStore;
  @Inject
  ActivityController controller;
  @Inject
  protected Cleanup cleanup;

  private String fileStoreDir;
  private static final String md5 = DigestUtils.md5Hex("hello world");
  private static final String content = "hello world";

  @Before
  public void setUp() throws Exception {
    controller.startOrResume(new ActivityHint(AddThoughtActivity.class));

    cleanup.cleanup();
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

  @After
  public void tearDown() throws Exception {
    controller.stopAll();
  }

  protected File createTmpFile() throws IOException {
    String tempFile = TMPDIR + File.separator + "input.txt";
    log.info("Creating tempfile {}", tempFile);
    File file = new File(tempFile);
    file.createNewFile();
    file.deleteOnExit();
    com.google.common.io.Files.write(content, file, Charsets.US_ASCII);
    return file;
  }

  @Test
  public void testSaveFileWithThought() throws Exception {
    final File file = createTmpFile();

    FileReference fileReference = PersistentWork.read(em -> {
      Thought bla = new Thought("bla");
      em.persist(bla);
      FileReference ref = null;
      try {
        ref = fileStore.getReference(file).get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
      bla.getFiles().add(ref);
      fileStore.scheduleCopy(ref, file);
      return ref;
    });

    assertNotNull(fileReference.getMd5Sum());
    assertEquals(fileReference.getMd5Sum(), md5);
    log.info("Generated md5sum {}", fileReference.getMd5Sum());

    String expectedFileStorePath = fileReference.getMd5Sum();
    assertTrue(new File(fileStoreDir + File.separator + expectedFileStorePath).exists());

    File reloaded = fileStore.getFile(fileReference);
    assertTrue(reloaded + " does not exist", reloaded.exists());

    PersistentWork.wrap(() -> {
      Thought thought = PersistentWork.forName(Thought.class, "bla");
      Set<FileReference> files = thought.getFiles();
      assertEquals(1, files.size());
      assertEquals(file.getName(), files.iterator().next().getName());
    });
  }

  @Test
  public void testExistingFile() throws Exception {
    File file = createTmpFile();

    Thought bla = new Thought("bla");
    PersistentWork.persist(bla);
    FileReference fileReference = fileStore.getReference(file).get();
    PersistentWork.run(em -> {
      Thought reload = PersistentWork.reload(bla);
      reload.addFileReference(fileReference);
      fileStore.scheduleCopy(fileReference, file);
      em.persist(fileReference);
    });

    com.google.common.io.Files.write("hello sauerland", file, Charsets.US_ASCII);

    FileReference newReference = fileStore.getReference(file).get();
    fileStore.saveInFileStore(newReference, file);

    assertEquals(fileReference.getId(), newReference.getId());
    assertNotEquals(fileReference.getMd5Sum(), newReference.getMd5Sum());
    File fileStoreFile = fileStore.getFile(newReference);

    List<String> lines = com.google.common.io.Files.readLines(fileStoreFile, Charsets.US_ASCII);
    assertEquals(1, lines.size());
    assertEquals("hello sauerland", lines.get(0));
  }

  @Test
  public void testGetFileReferences() throws Exception {
    Path path = Files.createTempFile("img", ".jpg");


    URL resource = getClass().getResource("/de/ks/idnadrev/entity/img.jpg");
    Path src = Paths.get(resource.toURI());
    Files.copy(src, path, StandardCopyOption.REPLACE_EXISTING);
    File file = path.toFile();
    file.deleteOnExit();

    FileReference fileReference = fileStore.getReference(file).get();
    PersistentWork.run(em -> {
      fileStore.scheduleCopy(fileReference, file);
      em.persist(fileReference);
    });
    assertThat(fileReference.getMimeType(), containsString("image"));

    List<FileReference> references = fileStore.getFilesByMimeType(MediaType.ANY_IMAGE_TYPE);
    assertEquals(1, references.size());

    references = fileStore.getFilesByMimeType(MediaType.ANY_AUDIO_TYPE);
    assertEquals(0, references.size());

    references = fileStore.getFilesByMimeType(MediaType.ANY_TYPE);
    assertEquals(1, references.size());
  }
}
