/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
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

package de.ks.persistence;

import de.ks.persistence.entity.AbstractPersistentObject;
import de.ks.persistence.entity.IdentifyableEntity;
import de.ks.persistence.entity.NamedPersistentObject;
import de.ks.persistence.transaction.SimpleTransaction;
import de.ks.persistence.transaction.TransactionProvider;
import de.ks.persistence.transaction.Transactional;
import de.ks.reflection.PropertyPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Handles transactions and {@link EntityManager} closing.
 * Usually used as an anonymous class.
 */
public class PersistentWork {
  private static volatile CDI cdi;

  static <T> CDI<T> getCdi() {
    if (cdi == null) {
      synchronized (PersistentWork.class) {
        if (cdi == null) {
          cdi = CDI.current();
        }
      }
    }
    return cdi;
  }

  private static final Logger log = LoggerFactory.getLogger(PersistentWork.class);

  public static <R> R wrap(Supplier<R> supplier) {
    return PersistentWork.read(em -> supplier.get());
  }

  public static void wrap(Runnable runnable) {
    PersistentWork.run(em -> runnable.run());
  }

  public static CompletableFuture<Void> runAsync(Consumer<EntityManager> consumer, ExecutorService executorService) {
    CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> run(consumer), executorService);
    return completableFuture;
  }

  public static <T> CompletableFuture<T> readAsync(Function<EntityManager, T> function, ExecutorService executorService) {
    CompletableFuture<T> completableFuture = CompletableFuture.supplyAsync(() -> read(function), executorService);
    return completableFuture;
  }

  public static void run(Consumer<EntityManager> consumer) {
    new PersistentWork(consumer).run();
  }

  public static <T> T read(Function<EntityManager, T> function) {
    return new PersistentWork(function).run();
  }

  public static <T> T reload(T instance) {
    return reload(instance, null);
  }

  public static <T> T reload(T instance, Consumer<T> resultWalker) {
    return read(em -> {
      if (instance instanceof AbstractPersistentObject) {
        AbstractPersistentObject apo = (AbstractPersistentObject) instance;
        @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) instance.getClass();
        T reloaded = em.<T>find(clazz, apo.getId());
        if (reloaded == null) {
          return instance;
        }
        if (resultWalker != null) {
          resultWalker.accept(reloaded);
        }
        return (T) reloaded;
      } else {
        return instance;
      }
    });
  }

  @SuppressWarnings("unchecked")
  public static <T> void merge(T... t) {
    merge(Arrays.asList(t));
  }

  public static <T> void merge(List<T> all) {
    run((em) -> all.forEach(em::merge));
  }

  public static <T> void persist(T... t) {
    persist(Arrays.asList(t));
  }

  public static <T> void persist(List<T> all) {
    run((em) -> all.forEach(t -> {
      em.persist(t);
    }));
  }

  public static <T> T byId(Class<T> clazz, Long id) {
    return read((em) -> em.find(clazz, id));
  }

  public static void deleteAllOf(Class<?>... classes) {
    deleteAllOf(Arrays.asList(classes));
  }

  public static void deleteAllOf(Collection<Class<?>> classes) {
    run((em) -> {
      for (Class<?> clazz : classes) {
        try {
          int deletedLines = em.createQuery("delete from " + clazz.getName()).executeUpdate();
          if (deletedLines > 0) {
            log.debug("Deleted {} from {}", deletedLines, clazz.getSimpleName());
          }
        } catch (Exception e) {
          log.error("Could not delete from {}", clazz.getSimpleName(), e);
          throw e;
        }
      }
    });
  }

  public static void deleteJoinTables(String... joinTables2) {
    deleteJoinTables(Arrays.asList(joinTables2));
  }

  public static void deleteJoinTables(Collection<String> joinTables) {
    run((em) -> {
      for (String joinTable : joinTables) {
        int deletedLines = em.createNativeQuery("delete from " + joinTable).executeUpdate();
        if (deletedLines > 0) {
          log.debug("Deleted {} from {}", deletedLines, joinTable);
        }
      }
    });
  }

  public static <T extends NamedPersistentObject<T>> T forName(Class<T> clazz, String name) {
    List<T> results = forName(clazz, name, false);
    if (results.size() != 1) {
      return null;
    } else {
      return results.get(0);
    }
  }

  public static <T extends NamedPersistentObject<T>> List<T> forNameLike(Class<T> clazz, String name) {
    return forName(clazz, name, true);
  }

  private static <T extends NamedPersistentObject<T>> List<T> forName(Class<T> clazz, String name, boolean like) {
    List<T> results = from(clazz, (Root<T> root, CriteriaQuery<T> query, CriteriaBuilder builder) -> {
      Predicate restriction;
      if (like) {
        restriction = builder.like(builder.lower(root.get("name")), name.toLowerCase(Locale.ROOT) + "%");
      } else {
        restriction = builder.equal(builder.lower(root.get("name")), name.toLowerCase(Locale.ROOT));
      }
      query.where(restriction);
    }, null);
    return results;
  }

  public static <T extends IdentifyableEntity> T findByIdentification(Class<T> clazz, String identifierProperty, Object identifier) {
    List<T> results = from(clazz, (Root<T> root, CriteriaQuery<T> query, CriteriaBuilder builder) -> {
      Predicate restriction = builder.equal(builder.lower(root.get(identifierProperty)), identifier);
      query.where(restriction);
    }, null);
    if (results.size() != 1) {
      return null;
    } else {
      return results.get(0);
    }
  }

  public static <T> List<T> from(Class<T> clazz) {
    return from(clazz, null, null);
  }

  public static <T> List<T> from(Class<T> clazz, Consumer<T> resultWalker) {
    return from(clazz, null, resultWalker);
  }

  public static <T> List<T> from(Class<T> clazz, QueryConsumer<T, T> consumer, Consumer<T> resultWalker) {
    return read((em) -> {
      CriteriaQuery<T> query = em.getCriteriaBuilder().createQuery(clazz);

      Root<T> root = query.from(clazz);
      query.select(root);

      if (consumer != null) {
        consumer.accept(root, query, em.getCriteriaBuilder());
      }

      List<T> resultList = em.createQuery(query).getResultList();
      if (resultWalker != null) {
        resultList.forEach(resultWalker);
      }
      return resultList;
    });
  }

  public static <T> List<Long> idsFrom(Class<T> clazz) {
    return idsFrom(clazz, null);
  }

  public static <T, V> List<V> projection(Class<T> clazz, boolean distinct, Function<T, V> resolver) {
    return projection(clazz, distinct, null, resolver, null);
  }

  @SuppressWarnings("unchecked")
  public static <T, V> List<V> projection(Class<T> clazz, boolean distinct, Function<T, V> resolver, QueryConsumer<T, V> queryConsumer) {
    return projection(clazz, distinct, null, resolver, queryConsumer);
  }

  public static <T, V> List<V> projection(Class<T> clazz, boolean distinct, Integer maxResults, Function<T, V> resolver, QueryConsumer<T, V> queryConsumer) {
    final String property = PropertyPath.property(clazz, t -> resolver.apply(t));

    return read((em) -> {
      CriteriaBuilder builder = em.getCriteriaBuilder();
      CriteriaQuery<Object> query = builder.createQuery();

      Root<T> root = query.from(clazz);
      Path<V> selection = root.get(property);
      query.distinct(distinct);
      query.select(selection);
      if (queryConsumer != null) {
        queryConsumer.accept(root, (CriteriaQuery<V>) query, builder);
      }

      TypedQuery<Object> emQuery = em.createQuery(query);
      if (maxResults != null) {
        emQuery.setMaxResults(maxResults);
      }
      @SuppressWarnings("unchecked") List<V> resultList = (List<V>) emQuery.getResultList();
      return resultList;
    });
  }

  public static <T> List<Long> idsFrom(Class<T> clazz, QueryConsumer<T, Long> consumer) {
    return read((em) -> {
      CriteriaQuery<Long> query = em.getCriteriaBuilder().createQuery(Long.class);

      Root<T> root = query.from(clazz);
      query.select(root.get("id"));

      if (consumer != null) {
        consumer.accept(root, query, em.getCriteriaBuilder());
      }

      List<Long> resultList = em.createQuery(query).getResultList();
      return resultList;
    });
  }

  protected static final ThreadLocal<EntityManager> localEM = new ThreadLocal<>();
  protected Consumer<EntityManager> consumer;
  protected Function<EntityManager, ?> function;

  private PersistentWork() {
  }

  private PersistentWork(Consumer<EntityManager> consumer) {
    this();
    this.consumer = consumer;
  }

  private PersistentWork(Function<EntityManager, ?> function) {
    this();
    this.function = function;
  }

  @SuppressWarnings("unchecked")
  protected <T> T run() {
    Optional<SimpleTransaction> txPesent = TransactionProvider.instance.getCurrentTransaction();
    if (localEM.get() != null && txPesent.isPresent()) {
      Object retval = execute();
      return (T) retval;
    } else {
      localEM.set(getCdi().select(EntityManager.class).get());
      Object run = Transactional.withNewTransaction(() -> {
        EntityManager em = localEM.get();
        TransactionProvider.instance.registerEntityManager(em);
        return execute();
      });
      return (T) run;
    }
  }

  protected Object execute() {
    if (consumer != null) {
      consumer.accept(localEM.get());
      return null;
    } else if (function != null) {
      return function.apply(localEM.get());
    }
    return null;
  }

  public static <T> long count(Class<T> clazz) {
    return count(clazz, null);
  }

  public static <T> long count(Class<T> clazz, QueryConsumer<T, Long> consumer) {
    return read(em -> {
      CriteriaQuery<Long> criteriaQuery = em.getCriteriaBuilder().createQuery(Long.class);
      Root<T> root = criteriaQuery.from(clazz);
      criteriaQuery.select(em.getCriteriaBuilder().count(root));
      if (consumer != null) {
        consumer.accept(root, criteriaQuery, em.getCriteriaBuilder());
      }
      return em.createQuery(criteriaQuery).getSingleResult();
    });
  }

  private static final String KEY_UPDATETIME = PropertyPath.property(AbstractPersistentObject.class, a -> a.getUpdateTime());

  public static <T extends AbstractPersistentObject<T>> LocalDateTime lastUpdate(Class<T> clazz) {
    return read(em -> {
      CriteriaQuery<LocalDateTime> criteriaQuery = em.getCriteriaBuilder().createQuery(LocalDateTime.class);
      Root<T> root = criteriaQuery.from(clazz);
      Path updateTime = root.get(KEY_UPDATETIME);
      criteriaQuery.orderBy(em.getCriteriaBuilder().desc(updateTime));
      criteriaQuery.select(updateTime);
      TypedQuery<LocalDateTime> query = em.createQuery(criteriaQuery);
      query.setMaxResults(1);
      List<LocalDateTime> resultList = query.getResultList();
      if (resultList.isEmpty()) {
        return null;
      } else {
        return resultList.get(0);
      }
    });
  }
}
