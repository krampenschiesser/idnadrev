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
package de.ks.launch;

import de.ks.flatadocdb.Repository;
import de.ks.flatjsondb.RepositorySelector;
import de.ks.idnadrev.repository.RepositoryLoader;
import de.ks.standbein.launch.Service;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class RepositoryService extends Service {
  @Inject
  RepositorySelector repositorySelector;
  @Inject
  RepositoryLoader loader;

  @Override
  protected void doStart() {
    List<Repository> repositories = loader.loadRepositories();
    repositorySelector.setRepositories(repositories);
  }

  @Override
  protected void doStop() {
    repositorySelector.getRepositories().forEach(Repository::close);
  }

  @Override
  public int getRunLevel() {
    return 0;
  }
}
