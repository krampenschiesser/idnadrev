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

import de.ks.idnadrev.entity.FileReference;
import de.ks.option.Options;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.entity.AbstractPersistentObject;
import de.ks.persistence.transaction.TransactionProvider;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class FileStore {
  private static final Logger log = LoggerFactory.getLogger(FileStore.class);
  private final FileOptions options;

  @Inject
  ExecutorService executorService;

  public FileStore() {
    options = Options.get(FileOptions.class);
  }

  public CompletableFuture<FileReference> getReference(AbstractPersistentObject owner, File file) {
    if (!file.exists()) {
      throw new IllegalArgumentException("File " + file + " does not exist");
    }
    CompletableFuture<String> md5Sum = CompletableFuture.supplyAsync(() -> getMd5(file), executorService);

    return md5Sum.thenApply(md5 -> resolveReference(md5, file));
  }

  protected FileReference resolveReference(String md5, File file) {
    FileReference fileReference = PersistentWork.forName(FileReference.class, file.getName());
    if (fileReference != null) {
      String originalMd5 = fileReference.getMd5Sum();
      if (!originalMd5.equals(md5)) {
        log.info("MD5Sum of file {} has changed from {} to {}", file.getName(), originalMd5, md5);
      }
      fileReference.setMd5Sum(md5);
      return fileReference;
    } else {
      FileReference reference = new FileReference(file.getName(), md5);
      return reference;
    }
  }

  public String getFileStoreInternalDir(AbstractPersistentObject specifier) {
    String folder = String.format("%09d", specifier.getId());
    return specifier.getClass().getSimpleName() + File.separator + folder;
  }

  private Path getTargetPath(AbstractPersistentObject specifier) {
    String fileStoreInternalDir = getFileStoreInternalDir(specifier);
    File file = new File(options.getFileStoreDir() + File.separator + fileStoreInternalDir);
    if (!file.exists()) {
      try {
        Files.createDirectories(file.toPath());
      } catch (IOException e) {
        log.error("Could not create file {}", file);
        throw new RuntimeException(e);
      }
    }
    return file.toPath();
  }

  protected String getMd5(File file) {
    try {
      return DigestUtils.md5Hex(new FileInputStream(file));
    } catch (IOException e) {
      log.error("Could not read md5 from file {}", file, e);
      throw new RuntimeException(e);
    }
  }

  public File getFile(FileReference fileReference) {
    AbstractPersistentObject owner = fileReference.getOwner();
    if (owner == null) {
      throw new IllegalArgumentException("owner of " + fileReference + " must not be null!");
    }
    Path targetDirectory = getTargetPath(owner);
    Path targetPath = targetDirectory.resolve(fileReference.getName());
    return targetPath.toFile();
  }

  public void scheduleCopy(FileReference fileReference, File file) {
    checkOwner(fileReference);
    String fileStoreInternalDir = getFileStoreInternalDir(fileReference.getOwner());
    fileReference.setFileStorePath(fileStoreInternalDir + File.separator + file.getName());

    CopyFileAfterCommit synchronization = new CopyFileAfterCommit(() -> {
      saveInFileStore(fileReference, file);
    });
    TransactionProvider.instance.getCurrentTransaction().ifPresent(tx -> {
      tx.registerSynchronization(synchronization);
    });
  }

  protected void checkOwner(FileReference fileReference) {
    if (fileReference.getOwner().getId() == 0) {
      throw new IllegalArgumentException("Owner " + fileReference.getOwner() + " has to be persisted");
    }
  }

  public void saveInFileStore(FileReference ref, File file) {
    checkOwner(ref);
    if (!file.exists()) {
      throw new IllegalArgumentException("File " + file + " has to exist");
    }
    if (ref.getFileStorePath() == null) {
      String fileStoreInternalDir = getFileStoreInternalDir(ref.getOwner());
      ref.setFileStorePath(fileStoreInternalDir + File.separator + file.getName());
    }

    Path targetPath = getTargetPath(ref.getOwner()).resolve(file.getName());
    if (options.shouldCopy()) {
      try {
        Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        log.error("could not copy {} to {}", file.toPath(), targetPath);
        throw new RuntimeException(e);
      }
    } else {
      try {
        Files.move(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        log.error("could not move {} to {}", file.toPath(), targetPath);
        throw new RuntimeException(e);
      }
    }
  }

  public String getFileStoreDir() {
    return Options.get(FileOptions.class).getFileStoreDir();
  }

  public String replaceFileStoreDir(String description) {
    String replacement = "file://" + getFileStoreDir();
    if (!replacement.endsWith(File.separator)) {
      replacement = replacement + File.separator;
    }
    String newDescription = StringUtils.replace(description, FileReference.FILESTORE_VAR, replacement);
    return newDescription;
  }
}