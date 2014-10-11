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

import com.google.common.net.MediaType;
import de.ks.activity.executor.ActivityExecutor;
import de.ks.idnadrev.entity.FileReference;
import de.ks.option.Options;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.transaction.TransactionProvider;
import de.ks.reflection.PropertyPath;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.criteria.Predicate;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FileStore {
  private static final Logger log = LoggerFactory.getLogger(FileStore.class);
  private static final String KEY_MIMETYPE = PropertyPath.property(FileReference.class, r -> r.getMimeType());
  private final FileOptions options;

  @Inject
  ActivityExecutor executorService;

  public FileStore() {
    options = Options.get(FileOptions.class);
  }

  public CompletableFuture<FileReference> getReference(File file) {
    if (!file.exists()) {
      throw new IllegalArgumentException("File " + file + " does not exist");
    }
    CompletableFuture<String> md5Sum = CompletableFuture.supplyAsync(() -> getMd5(file), executorService);

    return md5Sum.thenApply(md5 -> resolveReference(md5, file));
  }

  protected FileReference resolveReference(String md5, File file) {
    String mimeType = getMimeType(file);
    long size = getFileSize(file);

    FileReference fileReference = PersistentWork.forName(FileReference.class, file.getName());
    if (fileReference != null) {
      String originalMd5 = fileReference.getMd5Sum();
      if (!originalMd5.equals(md5)) {
        log.info("MD5Sum of file {} has changed from {} to {}", file.getName(), originalMd5, md5);
      }
      fileReference.setMd5Sum(md5);
      fileReference.setMimeType(mimeType);
      fileReference.setSizeInBytes(size);
      return fileReference;
    } else {
      FileReference reference = new FileReference(file.getName(), md5);
      reference.setSizeInBytes(size);
      reference.setMimeType(mimeType);
      return reference;
    }
  }

  private long getFileSize(File file) {
    try {
      return Files.size(file.toPath());
    } catch (IOException e) {
      log.error("Could not get filesize from {}", file, e);
      return -1;
    }
  }

  private String getMimeType(File file) {
    Path path = file.toPath();
    try {
      return Files.probeContentType(path);
    } catch (IOException e) {
      log.error("Could not get mime type from ", file, e);
      return null;
    }
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
    Path path = Paths.get(getFileStoreDir(), fileReference.getMd5Sum(), fileReference.getName());
    return path.toFile();
  }

  public void scheduleCopy(FileReference reference, File file) {
    CopyFileAfterCommit synchronization = new CopyFileAfterCommit(() -> {
      saveInFileStore(reference, file);
    });
    TransactionProvider.instance.getCurrentTransaction().ifPresent(tx -> {
      tx.registerSynchronization(synchronization);
    });
  }

  public void saveInFileStore(FileReference ref, File file) {
    if (!file.exists()) {
      throw new IllegalArgumentException("File " + file + " has to exist");
    }
    if (ref.getMd5Sum() == null) {
      throw new IllegalArgumentException("MD5 sum has to be calculated");
    }

    Path dir = Paths.get(getFileStoreDir(), ref.getMd5Sum());
    try {
      Files.createDirectories(dir);
    } catch (IOException e) {
      log.error("Could not store create parent directory {}", dir, e);
      return;
    }

    Path targetPath = Paths.get(getFileStoreDir(), ref.getMd5Sum(), ref.getName());
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

  public List<FileReference> getFilesByMimeType(MediaType mediaType) {
    assert mediaType != null;

    List<FileReference> references = PersistentWork.from(FileReference.class, (root, query, builder) -> {
      javax.persistence.criteria.Path<String> mimeType = root.get(KEY_MIMETYPE);
      if (!mediaType.type().equals("*")) {
        String pattern = mediaType.type() + "%";
        Predicate like = builder.like(mimeType, pattern);
        query.where(like);
      }
    }, null);

    log.info("Found {} references for mimeType {}", references.size(), mediaType.type());
    return references;
  }
}