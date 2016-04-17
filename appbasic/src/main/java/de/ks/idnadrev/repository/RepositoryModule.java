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

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.OptionalBinder;
import de.ks.idnadrev.repository.manage.RepositoryActivity;
import de.ks.standbein.activity.InitialActivity;
import de.ks.standbein.launch.Service;

import java.nio.file.Path;
import java.util.List;

public class RepositoryModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Service.class).addBinding().to(RepositoryService.class);

    RepositoryLoader loader = new RepositoryLoader();
    List<Path> repositoryPaths = loader.loadRepositoryPaths();
    if (repositoryPaths.isEmpty()) {
      OptionalBinder.newOptionalBinder(binder(), InitialActivity.class).setBinding().toInstance(new InitialActivity(RepositoryActivity.class));
    }
  }
}
