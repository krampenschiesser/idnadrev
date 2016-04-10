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
package de.ks.idnadrev.repository.ui;

import de.ks.idnadrev.repository.RepositoryLoader;
import de.ks.idnadrev.repository.RepositoryService;
import de.ks.standbein.datasource.ListDataSource;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RepositoryDS implements ListDataSource<Path> {
  RepositoryLoader loader = new RepositoryLoader();

  @Inject
  RepositoryService repositoryService;

  @Override
  public List<Path> loadModel(Consumer<List<Path>> furtherProcessing) {
    List<Path> paths = new ArrayList<>(loader.loadRepositoryPaths());
    furtherProcessing.accept(paths);
    return paths;
  }

  @Override
  public void saveModel(List<Path> model, Consumer<List<Path>> beforeSaving) {
    beforeSaving.accept(model);
    loader.addRepositories(model);
    repositoryService.reload();
  }
}
