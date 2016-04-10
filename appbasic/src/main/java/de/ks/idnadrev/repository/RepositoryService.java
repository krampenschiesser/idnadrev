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
package de.ks.idnadrev.repository;

import de.ks.standbein.launch.Service;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class RepositoryService extends Service {

  final Set<Repository> repositories = new HashSet<>();
  final RepositoryLoader loader;

  @Inject
  public RepositoryService(RepositoryLoader loader) {
    this.loader = loader;
  }

  @Override
  protected void doStart() {
    repositories.addAll(loader.loadRepositories());
  }

  @Override
  protected void doStop() {
//    repositorySelector.getRepositories().forEach(Repository::close);
  }

  @Override
  public int getRunLevel() {
    return 0;
  }

  public Set<Repository> getRepositories() {
    return repositories;
  }

  public RepositoryService addRepository(Repository repository) {
    repositories.add(repository);
    return this;
  }

  public RepositoryService removeRepository(Repository repository) {
    repositories.remove(repository);
    return this;
  }

  public void reload() {
    repositories.clear();
    repositories.addAll(loader.loadRepositories());
  }
}
