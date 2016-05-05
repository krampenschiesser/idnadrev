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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class RepositoryService extends Service {
  private static final Logger log = LoggerFactory.getLogger(RepositoryService.class);
  final Set<Repository> repositories = new HashSet<>();
  final AtomicReference<Repository> activeRepository = new AtomicReference<>();
  final RepositoryLoader loader;
  private final Scanner scanner;

  @Inject
  public RepositoryService(RepositoryLoader loader, Scanner scanner) {
    this.loader = loader;
    this.scanner = scanner;
  }

  @Override
  protected void doStart() {
    repositories.addAll(loader.loadRepositories());
    if (!repositories.isEmpty()) {
      setActiveRepository(repositories.iterator().next());
    }
    repositories.forEach(r -> scanner.scan(r, (ProgressCallback) (count, max) -> log.info("Scanned {}/{} repositories", count, max)));
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

  public RepositoryService setActiveRepository(Repository activeRepository) {
    this.activeRepository.set(activeRepository);
    return this;
  }

  public Repository getActiveRepository() {
    return activeRepository.get();
  }
}
