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
package de.ks.flatjsondb;

import de.ks.flatadocdb.Repository;
import de.ks.flatadocdb.session.SessionFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class RepositorySelector {
  final Set<Class> entityClasses;
  final AtomicReference<SessionFactory> sessionFactory = new AtomicReference<>();
  Repository currentRepository;
  private Set<Repository> repositories;

  @Inject
  public RepositorySelector(@RegisteredEntity Set<Class> entityClasses) {
    this.entityClasses = entityClasses;
  }

  @com.google.inject.Inject(optional = true)
  public void setRepositories(Set<Repository> repositories) {
    this.repositories = repositories;
  }

  public boolean hasCurrentRepository() {
    return currentRepository != null;
  }

  public Repository getCurrentRepository() {
    if (currentRepository == null) {
      throw new IllegalStateException("No repository set.");
    }
    return currentRepository;
  }

  public SessionFactory getSessionFactory() {
    if (sessionFactory.get() == null) {
      throw new IllegalStateException("No repository set.");
    }
    return sessionFactory.get();
  }

  @com.google.inject.Inject(optional = true)//in test we can register a default repository, yeah!
  public void setCurrentRepository(@InitialRepository Repository currentRepository) {
    this.currentRepository = currentRepository;


    if (sessionFactory.get() == null) {
      synchronized (this) {
        SessionFactory factory = new SessionFactory(currentRepository, entityClasses.toArray(new Class[entityClasses.size()]));
        this.sessionFactory.set(factory);
      }
    } else {
      sessionFactory.get().addRepository(currentRepository);
    }
  }
}
