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
package de.ks.idnadrev.index;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class IndexModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder<Query> queryBinder = Multibinder.newSetBinder(binder(), Query.class);
    queryBinder.addBinding().toInstance(StandardQueries.byTagsQuery());
    queryBinder.addBinding().toInstance(StandardQueries.contextQuery());
    queryBinder.addBinding().toInstance(StandardQueries.crontabQuery());
    queryBinder.addBinding().toInstance(StandardQueries.finishedQuery());
    queryBinder.addBinding().toInstance(StandardQueries.stateQuery());
    queryBinder.addBinding().toInstance(StandardQueries.titleQuery());
  }
}
