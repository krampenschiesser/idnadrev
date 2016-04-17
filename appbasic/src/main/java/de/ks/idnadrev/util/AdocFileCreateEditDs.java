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
package de.ks.idnadrev.util;

import com.google.common.base.StandardSystemProperty;
import de.ks.idnadrev.adoc.AdocFile;
import de.ks.idnadrev.repository.Repository;
import de.ks.idnadrev.repository.RepositoryService;
import de.ks.standbein.datasource.DataSource;
import de.ks.util.DeleteDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AdocFileCreateEditDs<E extends AdocFile> implements DataSource<E> {
  protected final Class<E> clazz;
  private final Function<Path, E> newInstance;
  private final RepositoryService repositoryService;
  protected E hint;
  private final Path tempFolder;

  public AdocFileCreateEditDs(Class<E> clazz, String tempFolderName, Function<Path, E> newInstance, RepositoryService repositoryService) {
    this.clazz = clazz;
    this.newInstance = newInstance;
    this.repositoryService = repositoryService;
    String tmpDir = StandardSystemProperty.JAVA_IO_TMPDIR.value();
    tempFolder = Paths.get(tmpDir, tempFolderName);
    cleanupTempDir();
  }

  protected void cleanupTempDir() {
    boolean exists = Files.exists(tempFolder);
    if (exists) {
      new DeleteDir(tempFolder).setDeleteFolder(false).delete();
    } else {
      try {
        Files.createDirectory(tempFolder);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public E loadModel(Consumer<E> furtherProcessing) {
    if (hint == null) {
      cleanupTempDir();
      Path path = tempFolder.resolve("temp.adoc");
      E retval = newInstance.apply(path);
      furtherProcessing.accept(retval);
      return retval;
    } else {
      furtherProcessing.accept(hint);
      return hint;
    }
  }

  @Override
  public void saveModel(E model, Consumer<E> beforeSaving) {
    Repository activeRepository = repositoryService.getActiveRepository();
    beforeSaving.accept(model);
    String content = model.writeBack();
    try {
      Files.write(model.getPath(), Collections.singleton(content), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
//    activeRepository.getPath().resolve("Thought").resolve() // FIXME: 4/17/16 copy folder to repo
  }

  @Override
  @SuppressWarnings("unchecked")
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint != null && clazz.isAssignableFrom(dataSourceHint.getClass())) {
      hint = ((E) dataSourceHint);
    } else {
      hint = null;
    }
  }

}
