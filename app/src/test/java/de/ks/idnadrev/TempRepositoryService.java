/*
 * Copyright [2015] [Christian Loehnert]
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

package de.ks.idnadrev;

import de.ks.flatadocdb.Repository;
import de.ks.flatadocdb.util.DeleteDir;
import de.ks.flatjsondb.RepositorySelector;
import de.ks.idnadrev.repository.RepositoryLoader;
import de.ks.standbein.launch.Service;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

@Singleton
public class TempRepositoryService extends Service {
  @Inject
  RepositorySelector repositorySelector;
  @Inject
  RepositoryLoader loader;

  private Path repoPath;
  private Repository repository;

  @Override
  protected void doStart() {
    try {
      repoPath = Files.createTempDirectory("idnadrevTempRepo");

      repository = new Repository(repoPath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    repositorySelector.setRepositories(Collections.singletonList(repository));
  }

  @Override
  protected void doStop() {
    repositorySelector.getRepositories().forEach(Repository::close);
    new DeleteDir(repoPath).delete();
    try {
      Files.deleteIfExists(repoPath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int getRunLevel() {
    return 0;
  }
}
