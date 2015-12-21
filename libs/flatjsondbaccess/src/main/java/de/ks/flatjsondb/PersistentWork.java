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
import de.ks.flatadocdb.entity.BaseEntity;
import de.ks.flatadocdb.entity.NamedEntity;
import de.ks.flatadocdb.index.IndexElement;
import de.ks.flatadocdb.session.Session;
import de.ks.flatadocdb.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PersistentWork {
  private static final Logger log = LoggerFactory.getLogger(PersistentWork.class);
  private final ThreadLocal<Session> localSession = new ThreadLocal<>();
  private final SessionFactory factory;
  private final Repository repository;

  @Inject
  public PersistentWork(SessionFactory factory, Repository repository) {
    this.factory = factory;
    this.repository = repository;
  }

  public void run(Consumer<Session> runnable) {
    read(session -> {
      runnable.accept(session);
      return null;
    });
  }

  public <E> E read(Function<Session, E> function) {
    if (localSession.get() != null) {
      return function.apply(localSession.get());
    } else {
      try {
        return factory.transactedSessionRead(repository, session -> {
          localSession.set(session);
          return function.apply(session);
        });
      } finally {
        localSession.set(null);
      }
    }
  }

  public <E> List<E> from(Class<E> root) {
    return read(session -> {
      Collection<IndexElement> elements = session.getRepository().getIndex().getAllOf(root);
      return elements.stream().map(e -> session.findById(e.getId())).map(o -> (E) o).collect(Collectors.toList());
    });
  }

  public <E extends BaseEntity> void remove(E entity) {
    run(session -> {
      if (entity.getId() == null) {
        log.warn("Trying to delete entity that is not persisted.");
      } else {
        E reloaded = session.findById(entity.getId());
        session.remove(reloaded);
      }
    });
  }

  public <E> void persist(E entity) {
    run(session -> session.persist(entity));
  }

  public void persist(Object... objects) {
    run(session -> {
      Arrays.asList(objects).forEach(session::persist);
    });
  }

  public <E extends BaseEntity> void removeAllOf(Class<E> root) {
    run(session -> {
      List<E> items = from(root);
      for (E item : items) {
        remove(item);
      }
    });
  }

  public <E extends BaseEntity> E reload(E entity) {
    return read(session -> {
      if (entity.getId() == null) {
        return entity;
      } else {
        return session.findById(entity.getId());
      }
    });
  }

  public <E extends NamedEntity> E forName(Class<E> clazz, String name) {
    return read(session -> session.findByNaturalId(clazz, name));
  }

  public <E, V> List<V> projection(Class<E> clazz, Function<E, V> projection) {
    return read(session -> from(clazz).stream().map(projection).collect(Collectors.toList()));
  }

  public <E> E byId(String id) {
    return read(session -> session.findById(id));
  }

  public void deleteAllOf(Class<?> clazz) {
    run(session -> {
      Collection<IndexElement> all = session.getRepository().getIndex().getAllOf(clazz);
      all.forEach(session::remove);
    });
  }
}
