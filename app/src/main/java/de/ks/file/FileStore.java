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
import org.apache.commons.codec.digest.DigestUtils;
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
    CompletableFuture<String> save = CompletableFuture.supplyAsync(() -> saveInFileStore(owner, file), executorService);

    return md5Sum.thenCombine(save, (md5, filePath) -> resolveReference(md5, filePath, owner, file));
  }

  public FileReference resolveReference(String md5, String fileStorePath, AbstractPersistentObject owner, File file) {
    FileReference fileReference = PersistentWork.forName(FileReference.class, file.getName());
    if (fileReference != null) {
      String originalMd5 = fileReference.getMd5Sum();
      if (!originalMd5.equals(md5)) {
        log.info("MD5Sum of file {} has changed from {} to {}", file.getName(), originalMd5, md5);
      }
      fileReference.setMd5Sum(md5);
      return fileReference;
    } else {
      FileReference reference = new FileReference(owner, file.getName(), md5).setFileStorePath(fileStorePath);
      PersistentWork.persistAndReload(reference, owner);
      return reference;
    }
  }

  public String getFileStoreInternalDir(AbstractPersistentObject specifier) {
    String folder = String.format("%09d", specifier.getId());
    return specifier.getClass().getSimpleName() + File.separator + folder;
  }

  protected String saveInFileStore(AbstractPersistentObject specifier, File file) {
    Path targetDirectory = getTargetPath(specifier);
    Path targetPath = targetDirectory.resolve(file.getName());
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
    return getFileStoreInternalDir(specifier) + File.separator + file.getName();
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
    Path targetDirectory = getTargetPath(fileReference.getOwner());
    Path targetPath = targetDirectory.resolve(fileReference.getName());
    return targetPath.toFile();
  }
}