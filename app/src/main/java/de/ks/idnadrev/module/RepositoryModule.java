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
package de.ks.idnadrev.module;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.OptionalBinder;
import de.ks.flatadocdb.Repository;
import de.ks.flatjsondb.InitialRepository;
import de.ks.idnadrev.repository.RepositoryActivity;
import de.ks.idnadrev.repository.RepositoryLoader;
import de.ks.launch.DummyData;
import de.ks.standbein.activity.InitialActivity;

import java.util.List;

public class RepositoryModule extends AbstractModule {
  @Override
  protected void configure() {
    boolean createDummy = Boolean.getBoolean(DummyData.CREATE_DUMMYDATA);
    if (createDummy) {
      OptionalBinder.newOptionalBinder(binder(), InitialActivity.class).setBinding().toInstance(new InitialActivity(RepositoryActivity.class));
    } else {
      RepositoryLoader loader = new RepositoryLoader();
      List<Repository> repositories = loader.loadRepositories();

      if (!repositories.isEmpty()) {
        Repository initial = repositories.get(0);
        bind(Repository.class).annotatedWith(InitialRepository.class).toInstance(initial);
      } else {
        OptionalBinder.newOptionalBinder(binder(), InitialActivity.class).setBinding().toInstance(new InitialActivity(RepositoryActivity.class));
      }
      Multibinder<Repository> repoBinder = Multibinder.newSetBinder(binder(), Repository.class);
      for (Repository repository : repositories) {
        repoBinder.addBinding().toInstance(repository);
      }
    }
  }
}
